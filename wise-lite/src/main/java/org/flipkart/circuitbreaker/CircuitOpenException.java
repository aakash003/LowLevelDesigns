package org.flipkart.circuitbreaker;

/** Thrown when circuit is OPEN and cooldown not yet elapsed. */
public class CircuitOpenException extends RuntimeException {
    public CircuitOpenException(String service) {
        super("Circuit OPEN for '" + service + "' — fail fast.");
    }
}
