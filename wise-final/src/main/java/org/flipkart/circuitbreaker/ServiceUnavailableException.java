package org.flipkart.circuitbreaker;

/**
 * Thrown by WebClient.execute() when the circuit breaker is OPEN
 * or HALF_OPEN with a probe already in flight.
 *
 * Unchecked so callers can catch explicitly without forced declaration.
 * Callers should catch this and apply a fallback strategy.
 */
public class ServiceUnavailableException extends RuntimeException {

    private final String service;
    private final CircuitBreakerState.State circuitState;

    public ServiceUnavailableException(String service,
                                       CircuitBreakerState.State circuitState,
                                       String message) {
        super(message);
        this.service      = service;
        this.circuitState = circuitState;
    }

    public String                    getService()      { return service; }
    public CircuitBreakerState.State getCircuitState() { return circuitState; }
}
