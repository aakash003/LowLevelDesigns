package org.flipkart.circuitbreaker;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * WebClient wraps a pluggable HTTP transport with per-service circuit breakers.
 *
 * Design decisions:
 *   - Per-service CircuitBreakerConfig → each service has its own rulebook.
 *   - Unknown services fall back to DEFAULT_CONFIG automatically.
 *   - Transport is Function<Request, Response> → easy to mock in tests.
 *   - Clock is injectable → time-based transitions testable without Thread.sleep().
 *   - Breakers are created lazily via ConcurrentHashMap.computeIfAbsent.
 */
public class WebClient {

    // ── fallback default config ───────────────────────────────────────────
    private static final CircuitBreakerConfig DEFAULT_CONFIG
        = CircuitBreakerConfig.defaultConfig();

    // ── registries ────────────────────────────────────────────────────────
    private final Map<String, CircuitBreakerConfig> configs;
    private final Map<String, CircuitBreaker>       breakers
        = new ConcurrentHashMap<>();

    private final Function<Request, Response> transport;
    private final Clock                       clock;

    // ── constructors ──────────────────────────────────────────────────────

    /** Production: all services use DEFAULT_CONFIG. */
    public WebClient(Function<Request, Response> transport) {
        this(transport, Map.of(), Clock.systemUTC());
    }

    /** Production: per-service config map. */
    public WebClient(Function<Request, Response> transport,
                     Map<String, CircuitBreakerConfig> configs) {
        this(transport, configs, Clock.systemUTC());
    }

    /** Tests: injectable clock forwarded to every CircuitBreaker created. */
    public WebClient(Function<Request, Response> transport,
                     Map<String, CircuitBreakerConfig> configs,
                     Clock clock) {
        this.transport = transport;
        this.configs   = Map.copyOf(configs);      // defensive copy — immutable
        this.clock     = clock;
    }

    // ── core execute method ───────────────────────────────────────────────

    /**
     * Send request to its downstream service, honouring that service's
     * circuit breaker.
     *
     * Flow:
     *   ① preRequest()  — throws CircuitOpenException if OPEN
     *   ② transport     — real / simulated HTTP call
     *   ③ classify      — onSuccess() or onFailure()
     *
     * @throws CircuitOpenException if circuit is OPEN and cooldown not elapsed
     */
    public Response execute(Request request) {
        CircuitBreaker cb = breakerFor(request.service);

        // ① Gate check
        cb.preRequest();

        // ② HTTP call
        Response response;
        try {
            response = transport.apply(request);
        } catch (Exception e) {
            cb.onFailure();              // network error = failure
            throw e;
        }

        // ③ Record outcome — only 5xx trips the breaker
        if (response.isServerError()) {
            cb.onFailure();
        } else {
            cb.onSuccess();
        }

        return response;
    }

    /** Current circuit state — for health checks / metrics. */
    public CircuitBreaker.State stateFor(String service) {
        return breakerFor(service).getState();
    }

    // ── private helpers ───────────────────────────────────────────────────

    /**
     * Lazily creates a CircuitBreaker for the service using its registered
     * config, or DEFAULT_CONFIG if not registered.
     * Thread-safe via ConcurrentHashMap.computeIfAbsent.
     */
    private CircuitBreaker breakerFor(String service) {
        return breakers.computeIfAbsent(service, __ -> {
            CircuitBreakerConfig cfg = configs.getOrDefault(service, DEFAULT_CONFIG);

            System.out.printf("  [WebClient] Creating breaker for '%s' " +
                "— threshold=%d window=%dmin cooldown=%dmin%n",
                service,
                cfg.failureThreshold(),
                cfg.windowMillis()    / 60_000,
                cfg.cooldownMillis()  / 60_000);

            return new CircuitBreaker(
                cfg.failureThreshold(),
                cfg.windowMillis(),
                cfg.cooldownMillis(),
                clock
            );
        });
    }
}
