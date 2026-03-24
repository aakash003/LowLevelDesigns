package org.flipkart.circuitbreaker.test;

import org.flipkart.*;
import org.flipkart.circuitbreaker.client.WebClient;
import org.flipkart.circuitbreaker.exceptions.CircuitOpenException;
import org.flipkart.circuitbreaker.model.Request;
import org.flipkart.circuitbreaker.model.Response;
import org.flipkart.circuitbreaker.model.enums.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full behavioural test suite for WebClient +
 *
 * All time-based transitions are driven by a TestClock that can be advanced
 * in-process — no Thread.sleep() anywhere.
 *
 * Structure:
 *   ClosedState      — normal operation, failure counting
 *   OpenState        — blocking, cooldown, isolation
 *   HalfOpenState    — trial probe mechanics, recovery, re-trip
 *   SlidingWindow    — stale failure eviction
 *   EdgeCases        — network exceptions, unknown services
 */
public class WebClientCircuitBreakerTest {

    // ── shared fixtures ───────────────────────────────────────────────────────
    private static final String SERVICE   = "ServiceB";
    private static final String SERVICE_C = "ServiceC";

    private TestClock     clock;
    private AtomicInteger statusCodeRef;
    private WebClient client;

    @BeforeEach
    void setUp() {
        clock         = new TestClock(Instant.EPOCH);
        statusCodeRef = new AtomicInteger(200);
        client        = new WebClient(
            req -> new Response(statusCodeRef.get(), "body"),
            clock
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Execute a call with the given status code; swallow CircuitOpenException. */
    private void call(int status) {
        statusCodeRef.set(status);
        try {
            client.execute(new Request(SERVICE, "/test", clock.millis()));
        } catch (CircuitOpenException ignored) { }
    }

    /** Assert that the next call is blocked and throws CircuitOpenException. */
    private void assertBlocked() {
        statusCodeRef.set(200);
        assertThrows(CircuitOpenException.class,
            () -> client.execute(new Request(SERVICE, "/test", clock.millis())));
    }

    /** Assert that the next call is NOT blocked and returns successfully. */
    private void assertAllowed() {
        statusCodeRef.set(200);
        assertDoesNotThrow(
            () -> client.execute(new Request(SERVICE, "/test", clock.millis())));
    }

    private State state() {
        return client.stateFor(SERVICE);
    }

    private void tripBreaker() {
        call(500); call(500); call(500);
        assertEquals(State.OPEN, state(), "Precondition: breaker should be OPEN");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CLOSED state
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CLOSED state")
    class ClosedState {

        @Test
        @DisplayName("starts in CLOSED state")
        void startsInClosedState() {
            assertEquals(State.CLOSED, state());
        }

        @Test
        @DisplayName("allows requests with no failures")
        void allowsRequests_noFailures() {
            assertAllowed();
            assertEquals(State.CLOSED, state());
        }

        @Test
        @DisplayName("two failures do not trip the breaker")
        void twoFailures_doNotTrip() {
            call(500);
            call(500);
            assertEquals(State.CLOSED, state());
            assertAllowed();
        }

        @Test
        @DisplayName("exactly three failures trip the breaker (boundary)")
        void exactlyThreeFailures_tripsBreaker() {
            call(500); call(500);
            assertEquals(State.CLOSED, state()); // 2 = not yet

            call(500);
            assertEquals(State.OPEN, state());   // 3 = tripped
        }

        @Test
        @DisplayName("success resets failure window — failures before success are discarded")
        void success_resetsFailureWindow() {
            call(500); call(500);  // 2 failures
            call(200);             // success → window cleared
            call(500); call(500);  // 2 more failures — should NOT trip
            assertEquals(State.CLOSED, state());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OPEN state
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("OPEN state")
    class OpenState {

        @Test
        @DisplayName("blocks all requests when OPEN")
        void blocksRequests_whenOpen() {
            tripBreaker();
            assertBlocked();
            assertBlocked();
            assertBlocked();
        }

        @Test
        @DisplayName("does NOT transition before cooldown expires")
        void doesNotTransition_beforeCooldown() {
            tripBreaker();
            clock.advance(4 * 60_000L);   // 4 min — cooldown is 5 min
            assertBlocked();
            assertEquals(State.OPEN, state());
        }

        @Test
        @DisplayName("ServiceB tripping does NOT affect ServiceC")
        void serviceIsolation() {
            tripBreaker();
            assertEquals(State.OPEN,   client.stateFor(SERVICE));
            assertEquals(State.CLOSED, client.stateFor(SERVICE_C));

            // ServiceC calls still go through
            statusCodeRef.set(200);
            assertDoesNotThrow(
                () -> client.execute(new Request(SERVICE_C, "/test", clock.millis())));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HALF_OPEN state
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("HALF_OPEN state")
    class HalfOpenState {

        @BeforeEach
        void tripAndWait() {
            tripBreaker();
            clock.advance(5 * 60_000L + 1);  // just past the 5-min cooldown
        }

        @Test
        @DisplayName("transitions to HALF_OPEN after cooldown elapses")
        void transitionsToHalfOpen_afterCooldown() {
            // Just reading state triggers the OPEN → HALF_OPEN promotion.
            assertEquals(State.HALF_OPEN, state());
        }

        @Test
        @DisplayName("allows exactly one trial probe in HALF_OPEN")
        void allowsOnlyOneProbe() {
            statusCodeRef.set(200);
            // First request claims the trial slot.
            assertDoesNotThrow(
                () -> client.execute(new Request(SERVICE, "/test", clock.millis())));
            // Second request is rejected while trial is in flight.
            assertBlocked();
        }

        @Test
        @DisplayName("successful probe closes the circuit")
        void successfulProbe_closesBreaker() {
            statusCodeRef.set(200);
            assertDoesNotThrow(
                () -> client.execute(new Request(SERVICE, "/test", clock.millis())));

            assertEquals(State.CLOSED, state());
            assertAllowed();  // subsequent calls go through normally
        }

        @Test
        @DisplayName("failed probe immediately re-opens with fresh cooldown")
        void failedProbe_reOpensBreaker() {
            call(500);  // probe fails
            assertEquals(State.OPEN, state());

            // Still within the new cooldown — must be blocked.
            assertBlocked();

            // Advance past the new cooldown → HALF_OPEN again.
            clock.advance(5 * 60_000L + 1);
            assertEquals(State.HALF_OPEN, state());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sliding window
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Sliding window")
    class SlidingWindow {

        @Test
        @DisplayName("failures outside the 10-min window are evicted and do not count")
        void staleFailures_evicted() {
            call(500); call(500);              // 2 failures at T+0

            clock.advance(11 * 60_000L);       // advance past the 10-min window

            call(500);                         // 1 fresh failure — first two evicted
            assertEquals(State.CLOSED, state());  // should NOT trip
        }

        @Test
        @DisplayName("three failures spanning the window boundary only count fresh ones")
        void windowBoundary_onlyFreshFailuresCounted() {
            call(500);                         // T+0  — will be stale at T+11
            clock.advance(9 * 60_000L);
            call(500);                         // T+9  — still fresh at T+11
            clock.advance(2 * 60_000L);        // now at T+11
            call(500);                         // T+11 — first failure now stale

            // Only 2 failures in the window — should NOT trip.
            assertEquals(State.CLOSED, state());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Edge cases
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("network exception counts as a failure")
        void networkException_countsAsFailure() {
            WebClient throwingClient = new WebClient(
                req -> { throw new RuntimeException("connection refused"); },
                clock
            );

            for (int i = 0; i < 3; i++) {
                try {
                    throwingClient.execute(new Request(SERVICE, "/", clock.millis()));
                } catch (Exception ignored) { }
            }
            assertEquals(State.OPEN, throwingClient.stateFor(SERVICE));
        }

        @Test
        @DisplayName("unknown service has no circuit breaker — call goes through")
        void unknownService_passesThrough() {
            statusCodeRef.set(200);
            // "ServiceX" is not pre-registered — but computeIfAbsent creates one lazily.
            // The important thing is it starts CLOSED and does not throw.
            assertDoesNotThrow(
                () -> client.execute(new Request("ServiceX", "/test", clock.millis())));
            assertEquals(State.CLOSED, client.stateFor("ServiceX"));
        }

        @Test
        @DisplayName("4xx responses are NOT treated as server failures")
        void clientError_doesNotTripBreaker() {
            // 4xx = client error, not a server failure — breaker should stay CLOSED.
            call(400); call(404); call(422);
            assertEquals(State.CLOSED, state());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TestClock — controllable clock for deterministic time-based tests
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * A mutable Clock backed by a single Instant.
     * Call advance(ms) to move time forward without sleeping.
     */
    public static final class TestClock extends Clock {

        private Instant now;

        public TestClock(Instant start) {
            this.now = start;
        }

        /** Advance the clock by {@code millis} milliseconds. */
        void advance(long millis) {
            now = now.plusMillis(millis);
        }

        /** Set the clock to an absolute epoch-ms offset from EPOCH. */
        public void advanceTo(long epochMs) {
            now = Instant.ofEpochMilli(epochMs);
        }

        public long millis() {
            return now.toEpochMilli();
        }

        @Override public Instant        instant() { return now; }
        @Override public ZoneOffset     getZone() { return ZoneOffset.UTC; }
        @Override public Clock          withZone(java.time.ZoneId zone) { return this; }
    }
}
