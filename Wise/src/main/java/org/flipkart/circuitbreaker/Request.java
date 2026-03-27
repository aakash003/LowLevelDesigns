package org.flipkart.circuitbreaker;

/**
 * Represents an outbound HTTP request to a downstream service.
 */
public class Request {
    public final String service;
    public final String endpoint;

    public Request(String service, String endpoint) {
        this.service  = service;
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "Request{service='" + service + "', endpoint='" + endpoint + "'}";
    }
}
