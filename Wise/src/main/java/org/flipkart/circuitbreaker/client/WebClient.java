package org.flipkart.circuitbreaker.client;

import org.flipkart.circuitbreaker.exceptions.CircuitOpenException;
import org.flipkart.circuitbreaker.model.Request;
import org.flipkart.circuitbreaker.model.Response;
import org.flipkart.circuitbreaker.model.enums.State;
import org.flipkart.circuitbreaker.service.CircuitBreaker;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * WebClient wraps a pluggable HTTP transport with per-service circuit breakers.
 *
 * Configuration (matching the problem statement):
 *   Failure threshold : 3 failures inside a 10-minute sliding window
 *   Cooldown period   : 5 minutes (OPEN state duration before HALF_OPEN trial)
 *
 * Design decisions:
 *   - Transport is a {@code Function<Request, Response>} so the breaker logic
 *     can be exercised in unit tests without a live network.
 *   - Clock is injectable so time-based transitions (OPEN → HALF_OPEN) are
 *     fully testable without Thread.sleep().
 *   - Circuit breakers are created lazily per service via computeIfAbsent —
 *     no up-front service registration required.
 */
public class WebClient {

    // ── circuit breaker settings ─────────────────────────────────────────────
    private static final int  FAILURE_THRESHOLD = 3;
    private static final long WINDOW_MILLIS     = 10 * 60 * 1_000L;   // 10 min
    private static final long COOLDOWN_MILLIS   =  5 * 60 * 1_000L;   //  5 min

    // ── per-service breaker registry ─────────────────────────────────────────
    private final Map<String, CircuitBreaker> breakersMap = new ConcurrentHashMap<>();

    /** The real (or simulated) HTTP transport. */
    private final Function<Request, Response> transport;

    /** Clock used by every CircuitBreaker this client creates. */
    private final Clock clock;
    // ── constructors ─────────────────────────────────────────────────────────

    /** Production constructor — uses the system UTC clock. */
    public WebClient(Function<Request, Response> transport) {
        this(transport, Clock.systemUTC());
    }

    /** Test constructor — inject a controllable clock. */
    public WebClient(Function<Request, Response> transport, Clock clock) {
        this.transport = transport;
        this.clock     = clock;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Core execute method
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Send {@code request} to its downstream service, honouring that service's
     * circuit breaker.
     *
     * Flow:
     *   ① preRequest()  — throws CircuitOpenException if blocked
     *   ② transport     — real / simulated HTTP call
     *   ③ classify      — onSuccess() or onFailure() updates breaker state
     *
     * @return the {@link Response} from the downstream service
     * @throws CircuitOpenException if the circuit is OPEN or HALF_OPEN-busy
     */
    public Response execute(Request request) {
        CircuitBreaker breaker = breakerFor(request.service);

        // ① Gate check — throws CircuitOpenException if not allowed
        breaker.preRequest();

        // ② Execute the actual (or simulated) HTTP call
        Response response;
        try {
            response = transport.apply(request);
        } catch (Exception e) {
            // Network-level exceptions count as failures.
            breaker.onFailure();
            throw e;
        }

        // ③ Record outcome
        if (response.isSuccess()) {
            breaker.onSuccess();
        } else {
            breaker.onFailure();
        }

        return response;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Observability
    // ─────────────────────────────────────────────────────────────────────────

    /** Current circuit state for a service (for health-checks / metrics). */
    public State stateFor(String service) {
        return breakerFor(service).getState();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Lazily creates a CircuitBreaker for {@code service} (thread-safe via
     * ConcurrentHashMap.computeIfAbsent).
     * The injected clock is forwarded so all breakers share the same time source.
     */
    private CircuitBreaker breakerFor(String service) {
        return breakersMap.computeIfAbsent(
            service,
            __ -> new CircuitBreaker(FAILURE_THRESHOLD, WINDOW_MILLIS,
                                     COOLDOWN_MILLIS, clock)
        );
    }
}
