package org.flipkart.circuitbreaker;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebClient — per-service circuit breakers, OPEN/CLOSED only.
 *
 * ── Design decisions ─────────────────────────────────────────
 *
 *   ConcurrentHashMap + computeIfAbsent:
 *     Breakers created lazily per service, exactly once,
 *     thread-safe — no explicit lock at registry level.
 *
 *   Clock injection:
 *     Single time source forwarded to every breaker.
 *     Swap TestClock in tests — no Thread.sleep().
 *
 *   Unknown service → defaults():
 *     Never throws on an unregistered service.
 *     Still circuit-breaker protected.
 *
 *   HTTP call made with NO lock held:
 *     HTTP can take seconds — holding a lock that long
 *     would stall every other thread. Lock released
 *     before send(), re-acquired inside recordFailure().
 *
 *   4xx does NOT trip the breaker:
 *     Client error — server is healthy.
 *     Only 5xx and network exceptions trip.
 */
public class WebClient {

    // ── registries ────────────────────────────────────────────
    private final ConcurrentHashMap<String, CircuitBreakerConfig> configs
        = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, CircuitBreaker> breakers
        = new ConcurrentHashMap<>();

    // ── dependencies ──────────────────────────────────────────
    private final HttpClient httpClient;
    private final Clock      clock;

    // ── constructors ──────────────────────────────────────────

    /** Production — system clock. */
    public WebClient(HttpClient httpClient) {
        this(httpClient, Clock.systemUTC());
    }

    /** Tests — injectable clock. */
    public WebClient(HttpClient httpClient, Clock clock) {
        this.httpClient = httpClient;
        this.clock      = clock;
    }

    /** Register per-service config before first request. */
    public void registerConfig(String service, CircuitBreakerConfig config) {
        configs.put(service, config);
    }

    // ── main entry point ──────────────────────────────────────

    /**
     * Execute request to downstream service.
     *
     * Flow:
     *   ① allowRequest() — fail fast if OPEN
     *   ② httpClient.send() — actual call (no lock held)
     *   ③ recordFailure / recordSuccess
     *
     * @throws CircuitOpenException if circuit is OPEN
     */
    public Response execute(Request request) {
        CircuitBreaker cb = breakerFor(request.service);

        // ① Gate check — fail fast
        if (!cb.allowRequest()) {
            throw new CircuitOpenException(request.service);
        }

        // ② HTTP call — no lock held here
        try {
            Response response = httpClient.send(request);

            // ③ Only 5xx trips the breaker — 4xx does not
            if (response.isServerError()) {
                cb.recordFailure();
            } else {
                cb.recordSuccess();
            }

            return response;

        } catch (Exception e) {
            // Network exception = server-side failure
            cb.recordFailure();
            throw e;
        }
    }

    /** Current state for a service — health check / metrics. */
    public CircuitBreaker.State stateFor(String service) {
        CircuitBreaker cb = breakers.get(service);
        return cb == null ? CircuitBreaker.State.CLOSED : cb.getState();
    }

    // ── private helpers ───────────────────────────────────────

    /**
     * Lazily create breaker with per-service config.
     * computeIfAbsent is atomic — created exactly once
     * even under concurrent load.
     */
    private CircuitBreaker breakerFor(String service) {
        return breakers.computeIfAbsent(service, svc -> {
            CircuitBreakerConfig cfg = configs.getOrDefault(
                svc, CircuitBreakerConfig.defaults());
            System.out.printf("  [WebClient] Breaker created for '%s' — %s%n",
                svc, cfg);
            return new CircuitBreaker(cfg, clock);
        });
    }
}
