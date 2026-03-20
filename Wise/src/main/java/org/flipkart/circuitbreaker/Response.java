package org.flipkart.circuitbreaker;

public class Response {
    public final int    statusCode;
    public final String body;

    public Response(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body       = body;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    @Override
    public String toString() {
        return "Response{status=" + statusCode + ", body='" + body + "'}";
    }
}
