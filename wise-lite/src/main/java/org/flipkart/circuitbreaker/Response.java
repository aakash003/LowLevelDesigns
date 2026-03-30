package org.flipkart.circuitbreaker;

/**
 * HTTP response.
 *
 * isServerError() → 5xx → trips the breaker
 * isSuccess()     → 2xx/4xx → does NOT trip
 *
 * 4xx = client error — server is healthy, don't penalise it.
 */
public class Response {
    final int    statusCode;
    final String body;

    public Response(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body       = body;
    }

    public boolean isServerError() { return statusCode >= 500; }
    public boolean isSuccess()     { return statusCode >= 200 && statusCode < 500; }

    @Override
    public String toString() {
        return "Response{" + statusCode + "}";
    }
}
