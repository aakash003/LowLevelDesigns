package org.flipkart.circuitbreaker;

/**
 * Abstraction over the actual HTTP transport.
 * Swap for a mock/stub in tests without a live network.
 */
@FunctionalInterface
public interface HttpClient {
    Response send(Request request);
}
