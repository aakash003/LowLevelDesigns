package org.flipkart.circuitbreaker;

/**
 * Outbound HTTP request to a downstream service.
 */
public class Request {
    private final String service;
    private final String endpoint;

    public Request(String service, String endpoint) {
        this.service  = service;
        this.endpoint = endpoint;
    }

    public String getServiceName() { return service; }
    public String getEndpoint()    { return endpoint; }

    @Override
    public String toString() {
        return "Request{service='" + service + "', endpoint='" + endpoint + "'}";
    }
}
