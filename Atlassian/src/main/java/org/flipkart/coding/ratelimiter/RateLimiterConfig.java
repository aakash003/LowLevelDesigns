package org.flipkart.coding.ratelimiter;

public class RateLimiterConfig {
    int countOfRequestAllowed;
    long timeStamp;
    int creditRequest;

    RateLimiterConfig(int countOfRequestAllowed, long timeStamp, int creditRequest) {
        this.countOfRequestAllowed = countOfRequestAllowed;
        this.timeStamp = timeStamp;
        this.creditRequest = creditRequest;
    }


    int getCountOfRequestAllowed() {
        return this.countOfRequestAllowed;
    }

    long getTimeStamp() {
        return this.timeStamp;
    }

    int getCreditRequest() {
        return this.creditRequest;
    }
}
