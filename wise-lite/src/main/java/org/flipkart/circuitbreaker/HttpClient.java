package org.flipkart.circuitbreaker;

/** Pluggable HTTP transport — swap for a mock in tests. */
@FunctionalInterface
public interface HttpClient {
    Response send(Request request);
}
