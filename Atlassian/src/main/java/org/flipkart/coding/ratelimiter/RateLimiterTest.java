package org.flipkart.coding.ratelimiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(5, 10); // Allow 5 requests per 10 seconds
    }

    @Test
    void testAllowRequestsWithinLimit() {
        int customerId = 1;
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.rateLimit(customerId), "Request should be allowed");
        }
    }

    @Test
    void testRejectRequestsExceedingLimit() {
        int customerId = 1;
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.rateLimit(customerId), "Request should be allowed");
        }
        assertFalse(rateLimiter.rateLimit(customerId), "Request should be rejected");
    }

    @Test
    void testAllowRequestsAfterTimeWindow() throws InterruptedException {
        int customerId = 1;
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.rateLimit(customerId), "Request should be allowed");
        }
        assertFalse(rateLimiter.rateLimit(customerId), "Request should be rejected");

        // Wait for the time window to pass
        Thread.sleep(10000);

        assertTrue(rateLimiter.rateLimit(customerId), "Request should be allowed after time window");
    }

    @Test
    void testCreditRequests() {
        int customerId = 1;
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.rateLimit(customerId), "Request should be allowed");
        }
        assertFalse(rateLimiter.rateLimit(customerId), "Request should be rejected");

        // Simulate credit requests
        rateLimiter.mapConfig.put(customerId, new RateLimiterConfig(5, System.currentTimeMillis(), 2));

        assertTrue(rateLimiter.rateLimit(customerId), "Request should be allowed using credit");
        assertTrue(rateLimiter.rateLimit(customerId), "Request should be allowed using credit");
        assertFalse(rateLimiter.rateLimit(customerId), "Request should be rejected after using all credits");
    }
}