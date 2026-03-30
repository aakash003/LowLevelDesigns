package org.flipkart.circuitbreaker;

/**
 * HTTP response from a downstream service.
 *
 * Classification:
 *   isServerError() → 5xx → trips the circuit breaker
 *   isSuccess()     → 2xx-4xx → does NOT trip
 *
 * Why not 4xx?
 *   4xx = client sent a bad request — server is healthy.
 *   Tripping on 4xx would penalise a healthy service
 *   for our own bad requests.
 */
public class Response {
    private final int    statusCode;
    private final String body;

    public Response(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body       = body;
    }

    public int    getStatusCode() { return statusCode; }
    public String getBody()       { return body; }

    /** Only 5xx counts as a server failure. */
    public boolean isServerError() { return statusCode >= 500; }

    /** 2xx + 4xx both count as success from breaker's perspective. */
    public boolean isSuccess()     { return statusCode >= 200 && statusCode < 500; }

    @Override
    public String toString() {
        return "Response{status=" + statusCode + ", body='" + body + "'}";
    }
}
