package org.flipkart.circuitbreaker;

public class Request {
    public final String service;       // e.g. "ServiceB", "ServiceC"
    public final String endpoint;      // arbitrary — passed through as-is
    public final long   timestampMs;   // simulated "wall clock" for the request

    public Request(String service, String endpoint, long timestampMs) {
        this.service     = service;
        this.endpoint    = endpoint;
        this.timestampMs = timestampMs;
    }
}
