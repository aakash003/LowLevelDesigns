package org.flipkart.circuitbreaker;

/**
 * Thrown by WebClient.execute() when the circuit breaker for a service is OPEN.
 * Unchecked so callers can catch it explicitly without declaration overhead.
 */
public class CircuitOpenException extends RuntimeException {
    public CircuitOpenException(String message) {
        super(message);
    }
}
