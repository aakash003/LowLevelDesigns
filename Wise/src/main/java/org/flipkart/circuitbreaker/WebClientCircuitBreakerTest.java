package org.flipkart.circuitbreaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests use a shared WebClient with a configurable transport so we can
 * control which status code comes back on each call.
 *
 * Important limitation: the CircuitBreaker uses Instant.now() internally,
 * so time-based transitions (OPEN → HALF_OPEN after cooldown) can't be
 * tested deterministically without injecting a Clock interface.
 *
 * Tests below cover all behavioural paths that don't require time travel.
 * A production codebase would inject a java.time.Clock for full coverage.
 */
class WebClientCircuitBreakerTest {

    private AtomicInteger statusCodeRef;
    private WebClient     client;

    private static final String SERVICE = "ServiceB";

    @BeforeEach
    void setUp() {
        statusCodeRef = new AtomicInteger(200);
        client = new WebClient(req -> new Response(statusCodeRef.get(), "body"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void call(int statusCode) {
        statusCodeRef.set(statusCode);
        try {
            client.execute(new Request(SERVICE, "/test", System.currentTimeMillis()));
        } catch (CircuitOpenException ignored) {
            // swallowed — caller uses stateFor() to inspect
        }
    }

    private void callExpectBlocked() {
        statusCodeRef.set(200);
        assertThrows(CircuitOpenException.class,
            () -> client.execute(new Request(SERVICE, "/test", System.currentTimeMillis())));
    }

    private CircuitBreaker.State state() {
        return client.stateFor(SERVICE);
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void startsInClosedState() {
        assertEquals(CircuitBreaker.State.CLOSED, state());
    }

    @Test
    void successKeepsBreakerClosed() {
        call(200);
        call(200);
        assertEquals(CircuitBreaker.State.CLOSED, state());
    }

    @Test
    void twiceFailureDoesNotTrip() {
        call(500);
        call(500);
        assertEquals(CircuitBreaker.State.CLOSED, state());
    }

    @Test
    void threeFailuresInWindowTripsBreaker() {
        call(500);
        call(500);
        call(500);
        assertEquals(CircuitBreaker.State.OPEN, state());
    }

    @Test
    void requestBlockedWhenOpen() {
        call(500);
        call(500);
        call(500);   // trips
        callExpectBlocked();
    }

    @Test
    void successAfterTripDoesNotResetImmediately() {
        // Trip, then try a success — but the circuit is OPEN so the success
        // call should be blocked, not let through.
        call(500); call(500); call(500);
        callExpectBlocked();  // blocked — no state change
        assertEquals(CircuitBreaker.State.OPEN, state());
    }

    @Test
    void successResetsFailureWindow() {
        call(500);
        call(500);
        call(200);   // resets window
        call(500);   // only 1 failure in window now
        call(500);   // only 2 failures
        assertEquals(CircuitBreaker.State.CLOSED, state());
    }

    @Test
    void separateServicesHaveIndependentBreakers() {
        AtomicInteger code = new AtomicInteger(500);
        WebClient sharedClient = new WebClient(req -> new Response(code.get(), "body"));

        // Trip ServiceB
        for (int i = 0; i < 3; i++) {
            try {
                sharedClient.execute(new Request("ServiceB", "/", System.currentTimeMillis()));
            } catch (CircuitOpenException ignored) {}
        }
        assertEquals(CircuitBreaker.State.OPEN, sharedClient.stateFor("ServiceB"));

        // ServiceC should still be CLOSED
        assertEquals(CircuitBreaker.State.CLOSED, sharedClient.stateFor("ServiceC"));
    }

    @Test
    void networkExceptionCountsAsFailure() {
        WebClient throwingClient = new WebClient(req -> {
            throw new RuntimeException("connection refused");
        });

        for (int i = 0; i < 3; i++) {
            try {
                throwingClient.execute(new Request(SERVICE, "/", System.currentTimeMillis()));
            } catch (Exception ignored) {}
        }
        assertEquals(CircuitBreaker.State.OPEN, throwingClient.stateFor(SERVICE));
    }
}
