package org.flipkart.circuitbreaker;

/**
 * Represents an HTTP response from a downstream service.
 *
 * isServerError() → 5xx  → counts as a circuit breaker failure
 * isSuccess()     → 2xx-4xx → does NOT trip the breaker
 *                             (4xx = client error, not server fault)
 */
public class Response {
    public final int    statusCode;
    public final String body;

    public Response(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body       = body;
    }

    public boolean isServerError() { return statusCode >= 500; }
    public boolean isSuccess()     { return statusCode >= 200 && statusCode < 500; }

    @Override
    public String toString() {
        return "Response{status=" + statusCode + ", body='" + body + "'}";
    }
}
