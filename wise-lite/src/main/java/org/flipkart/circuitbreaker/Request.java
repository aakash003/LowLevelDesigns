package org.flipkart.circuitbreaker;

/** Outbound HTTP request. */
public class Request {
    final String service;
    final String endpoint;

    public Request(String service, String endpoint) {
        this.service  = service;
        this.endpoint = endpoint;
    }
}
