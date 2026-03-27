package org.flipkart.circuitbreaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full behavioural test suite for WebClient + CircuitBreaker.
 *
 * All time-based transitions are driven by TestClock.advance() —
 * no Thread.sleep() anywhere in this suite.
 *
 * Coverage:
 *   ClosedState   — normal operation, failure counting, boundaries
 *   OpenState     — blocking, cooldown, service isolation
 *   SlidingWindow — stale failure eviction, window boundaries
 *   EdgeCases     — network exceptions, 4xx responses, unknown services
 *   PerServiceConfig — each service gets its own config
 */
class WebClientCircuitBreakerTest {

    private static final String SVC_B = "SVC_B";
    private static final String SVC_C = "SVC_C";

    private TestClock     clock;
    private AtomicInteger statusCode;
    private WebClient     client;

    @BeforeEach
    void setUp() {
        clock      = new TestClock(Instant.EPOCH);
        statusCode = new AtomicInteger(200);
        client     = new WebClient(
            req -> new Response(statusCode.get(), "body"),
            Map.of(SVC_B, CircuitBreakerConfig.builder()
                              .failureThreshold(3)
                              .windowMinutes(10)
                              .cooldownMinutes(5)
                              .build()),
            clock
        );
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private void call(int status) {
        statusCode.set(status);
        try { client.execute(new Request(SVC_B, "/test")); }
        catch (CircuitOpenException ignored) { }
    }

    private void assertBlocked() {
        statusCode.set(200);
        assertThrows(CircuitOpenException.class,
            () -> client.execute(new Request(SVC_B, "/test")));
    }

    private void assertAllowed() {
        statusCode.set(200);
        assertDoesNotThrow(
            () -> client.execute(new Request(SVC_B, "/test")));
    }

    private CircuitBreaker.State state() {
        return client.stateFor(SVC_B);
    }

    private void tripBreaker() {
        call(500); call(500); call(500);
        assertEquals(CircuitBreaker.State.OPEN, state(),
            "Precondition: breaker should be OPEN after 3 failures");
    }

    // ─────────────────────────────────────────────────────────────────────
    // CLOSED state
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CLOSED state")
    class ClosedState {

        @Test
        @DisplayName("starts in CLOSED state")
        void startsInClosedState() {
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test
        @DisplayName("allows requests with no failures")
        void allowsRequests_noFailures() {
            assertAllowed();
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test
        @DisplayName("two failures do not trip the breaker")
        void twoFailures_doNotTrip() {
            call(500); call(500);
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test
        @DisplayName("exactly three failures trip the breaker — boundary check")
        void exactlyThreeFailures_tripsBreaker() {
            call(500); call(500);
            assertEquals(CircuitBreaker.State.CLOSED, state()); // 2 — not yet

            call(500);
            assertEquals(CircuitBreaker.State.OPEN, state());   // 3 — tripped
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // OPEN state
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("OPEN state")
    class OpenState {

        @Test
        @DisplayName("blocks all requests when OPEN")
        void blocksRequests_whenOpen() {
            tripBreaker();
            assertBlocked();
            assertBlocked();
        }

        @Test
        @DisplayName("does NOT close before cooldown expires")
        void doesNotClose_beforeCooldown() {
            tripBreaker();
            clock.advance(4 * 60_000L);       // 4 min — cooldown is 5 min
            assertBlocked();
            assertEquals(CircuitBreaker.State.OPEN, state());
        }

        @Test
        @DisplayName("closes after cooldown expires")
        void closes_afterCooldown() {
            tripBreaker();
            clock.advance(5 * 60_000L + 1);   // just past 5 min
            assertAllowed();
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test
        @DisplayName("SVC_B tripping does NOT affect SVC_C")
        void serviceIsolation() {
            tripBreaker();
            assertEquals(CircuitBreaker.State.OPEN,   client.stateFor(SVC_B));
            assertEquals(CircuitBreaker.State.CLOSED, client.stateFor(SVC_C));

            statusCode.set(200);
            assertDoesNotThrow(
                () -> client.execute(new Request(SVC_C, "/test")));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Sliding window
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Sliding window")
    class SlidingWindow {

        @Test
        @DisplayName("failures outside the 10-min window are evicted — does NOT trip")
        void staleFailures_evicted() {
            call(500); call(500);               // 2 failures at T+0
            clock.advance(11 * 60_000L);        // past the 10-min window
            call(500);                          // 1 fresh failure — first two evicted
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test
        @DisplayName("isWithinWindow: failure exactly at boundary is evicted")
        void failureAtExactBoundary_evicted() {
            call(500);                          // T+0  fail 1
            call(500);                          // T+0  fail 2
            clock.advance(10 * 60_000L);        // advance to exactly T+10m
            call(500);                          // T+10m fail 3
            // evictStale: cutoff = T+10m - 10m = T+0
            // peekFirst = T+0 <= T+0 → EVICTED
            // window = [fail2@T+0, fail3@T+10m], size=2 < 3
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test
        @DisplayName("three fresh failures within window DOES trip")
        void threeFreshFailures_trips() {
            call(500);                          // T+0
            clock.advance(3 * 60_000L);
            call(500);                          // T+3
            clock.advance(3 * 60_000L);
            call(500);                          // T+6 — all within 10 min window
            assertEquals(CircuitBreaker.State.OPEN, state());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Edge cases
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("network exception counts as a failure")
        void networkException_countsAsFailure() {
            WebClient throwingClient = new WebClient(
                req -> { throw new RuntimeException("connection refused"); },
                Map.of(),
                clock
            );

            for (int i = 0; i < 3; i++) {
                try { throwingClient.execute(new Request(SVC_B, "/")); }
                catch (Exception ignored) { }
            }
            assertEquals(CircuitBreaker.State.OPEN, throwingClient.stateFor(SVC_B));
        }

        @Test
        @DisplayName("4xx responses do NOT trip the breaker — client error, not server fault")
        void clientError_doesNotTrip() {
            call(400); call(404); call(422);
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test
        @DisplayName("unknown service falls back to default config")
        void unknownService_usesDefaultConfig() {
            // SVC_X has no registered config — should use default and start CLOSED
            assertDoesNotThrow(
                () -> client.execute(new Request("SVC_X", "/test")));
            assertEquals(CircuitBreaker.State.CLOSED, client.stateFor("SVC_X"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Per-service config
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Per-service config")
    class PerServiceConfig {

        @Test
        @DisplayName("SVC_B (threshold=3) trips faster than SVC_C (threshold=10)")
        void perServiceConfig_differentThresholds() {
            Map<String, CircuitBreakerConfig> configs = Map.of(
                "SVC_B", CircuitBreakerConfig.builder()
                             .failureThreshold(3)
                             .windowMinutes(10)
                             .cooldownMinutes(5)
                             .build(),
                "SVC_C", CircuitBreakerConfig.builder()
                             .failureThreshold(10)
                             .windowMinutes(5)
                             .cooldownMinutes(2)
                             .build()
            );

            AtomicInteger sc = new AtomicInteger(500);
            WebClient     w  = new WebClient(
                req -> new Response(sc.get(), "body"), configs, clock);

            // Trip SVC_B with 3 failures
            for (int i = 0; i < 3; i++) {
                try { w.execute(new Request("SVC_B", "/")); }
                catch (CircuitOpenException ignored) { }
            }
            assertEquals(CircuitBreaker.State.OPEN, w.stateFor("SVC_B"));

            // SVC_C needs 10 failures — 3 should NOT trip it
            for (int i = 0; i < 3; i++) {
                try { w.execute(new Request("SVC_C", "/")); }
                catch (CircuitOpenException ignored) { }
            }
            assertEquals(CircuitBreaker.State.CLOSED, w.stateFor("SVC_C"));
        }
    }
}
