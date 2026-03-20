package org.flipkart.circuitbreaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * WebClient wraps a simulated HTTP transport with per-service circuit breakers.
 *
 * Configuration (matching the problem statement):
 *   - Failure threshold : 3 failures inside a 10-minute sliding window
 *   - Cooldown period   : 5 minutes (OPEN state duration before HALF_OPEN trial)
 *
 * The actual HTTP call is abstracted as a {@code Function<Request, Response>} so
 * the breaker logic can be exercised in unit tests without a live network.
 */
public class WebClient {

    // ── circuit breaker settings ─────────────────────────────────────────────
    private static final int  FAILURE_THRESHOLD    = 3;
    private static final long WINDOW_MILLIS        = 10 * 60 * 1000L;   // 10 min
    private static final long COOLDOWN_MILLIS      =  5 * 60 * 1000L;   //  5 min

    // ── per-service breaker registry ─────────────────────────────────────────
    private final Map<String, CircuitBreaker> breakersMap = new ConcurrentHashMap<>();

    /** The real (or simulated) HTTP transport. */
    private final Function<Request, Response> transport;

    public WebClient(Function<Request, Response> transport) {
        this.transport = transport;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Core execute method
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Send a request to {@code request.service}, respecting that service's
     * circuit breaker.
     *
     * @param request the outbound request (carries service name + timestamp)
     * @return the {@link Response} from the downstream service
     * @throws CircuitOpenException if the circuit is OPEN or HALF_OPEN-busy
     */
    public Response execute(Request request) {
        //fetch each service breaker from the registry (or create if missing)
        CircuitBreaker breaker = breakerFor(request.service);

        // ① Gate check — throws if the circuit is OPEN / trial already in-flight
        breaker.preRequest();

        Response response;
        try {
            // ② Actual call (simulated via injected transport)
            response = transport.apply(request);
        } catch (Exception e) {
            // Network-level exceptions count as failures too
            breaker.onFailure();
            throw e;
        }

        // ③ Classify the response
        if (response.isSuccess()) {
            breaker.onSuccess();
        } else {
            breaker.onFailure();
        }

        return response;
    }

    /** Expose breaker state for health-check / metrics endpoints. */
    public CircuitBreaker.State stateFor(String service) {
        return breakerFor(service).getState();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private CircuitBreaker breakerFor(String service) {
        return breakersMap.computeIfAbsent(
            service,
            __ -> new CircuitBreaker(FAILURE_THRESHOLD, WINDOW_MILLIS, COOLDOWN_MILLIS)
        );
    }
}
