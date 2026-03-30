package org.flipkart.circuitbreaker;

import org.junit.jupiter.api.*;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused test suite for the lite circuit breaker (OPEN/CLOSED only).
 * All time-based transitions use TestClock — no Thread.sleep().
 */
class WebClientTest {

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
            req -> new Response(statusCode.get(), "body"), clock);
        client.registerConfig(SVC_B, new CircuitBreakerConfig(
            3, 10 * 60_000L, 5 * 60_000L));
    }

    // ── helpers ───────────────────────────────────────────────

    private void call(int status) {
        statusCode.set(status);
        try { client.execute(new Request(SVC_B, "/test")); }
        catch (CircuitOpenException ignored) { }
    }

    private void assertBlocked() {
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
        assertEquals(CircuitBreaker.State.OPEN, state());
    }

    // ─────────────────────────────────────────────────────────
    // CLOSED STATE
    // ─────────────────────────────────────────────────────────

    @Nested @DisplayName("CLOSED state")
    class ClosedState {

        @Test @DisplayName("starts CLOSED")
        void startsClosed() {
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test @DisplayName("allows requests — no failures")
        void allowsRequests_noFailures() {
            assertAllowed();
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test @DisplayName("two failures do NOT trip")
        void twoFailures_doNotTrip() {
            call(500); call(500);
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test @DisplayName("exactly 3 failures trip — boundary")
        void exactlyThree_trips() {
            call(500); call(500);
            assertEquals(CircuitBreaker.State.CLOSED, state());   // 2 — not yet

            call(500);
            assertEquals(CircuitBreaker.State.OPEN, state());     // 3 — tripped
        }

        @Test @DisplayName("success does NOT reset failure window")
        void success_doesNotResetWindow() {
            call(500); call(500);
            call(200);   // success — window intentionally NOT cleared
            call(500);   // 3rd failure — should trip
            assertEquals(CircuitBreaker.State.OPEN, state());
        }
    }

    // ─────────────────────────────────────────────────────────
    // OPEN STATE
    // ─────────────────────────────────────────────────────────

    @Nested @DisplayName("OPEN state")
    class OpenState {

        @Test @DisplayName("blocks all requests when OPEN")
        void blocksAll_whenOpen() {
            tripBreaker();
            assertBlocked();
            assertBlocked();
        }

        @Test @DisplayName("does NOT close before cooldown")
        void doesNotClose_beforeCooldown() {
            tripBreaker();
            clock.advance(4 * 60_000L);   // 4 min — need 5
            assertBlocked();
            assertEquals(CircuitBreaker.State.OPEN, state());
        }

        @Test @DisplayName("closes after cooldown")
        void closes_afterCooldown() {
            tripBreaker();
            clock.advance(5 * 60_000L + 1);
            assertAllowed();
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test @DisplayName("SVC_B trip does NOT affect SVC_C")
        void serviceIsolation() {
            tripBreaker();
            assertEquals(CircuitBreaker.State.OPEN,   client.stateFor(SVC_B));
            assertEquals(CircuitBreaker.State.CLOSED, client.stateFor(SVC_C));

            statusCode.set(200);
            assertDoesNotThrow(
                () -> client.execute(new Request(SVC_C, "/test")));
        }
    }

    // ─────────────────────────────────────────────────────────
    // SLIDING WINDOW
    // ─────────────────────────────────────────────────────────

    @Nested @DisplayName("Sliding window")
    class SlidingWindow {

        @Test @DisplayName("stale failures evicted — does NOT trip")
        void staleFailures_evicted() {
            call(500); call(500);              // T+0: fail 1, 2
            clock.advance(11 * 60_000L);       // past 10-min window
            call(500);                         // T+11m: only 1 fresh
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test @DisplayName("failure at exact boundary is evicted")
        void exactBoundary_evicted() {
            call(500); call(500);              // T+0: fail 1, 2
            clock.advance(10 * 60_000L);       // exactly T+10m
            call(500);                         // T+10m: fail 3
            // cutoff = T+10m - 10m = T+0
            // both T+0 failures evicted (<=)
            // window = [fail3@T+10m], size=1 < 3 — NOT tripped
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test @DisplayName("evict BEFORE add — correct ordering")
        void evictBeforeAdd_ordering() {
            call(500); call(500);              // T+0: fail 1, 2
            clock.advance(10 * 60_000L + 1);   // T+10m+1ms — both stale
            call(500);                         // 1 fresh only after evict
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }
    }

    // ─────────────────────────────────────────────────────────
    // EDGE CASES
    // ─────────────────────────────────────────────────────────

    @Nested @DisplayName("Edge cases")
    class EdgeCases {

        @Test @DisplayName("4xx does NOT trip — client error not server fault")
        void clientError_doesNotTrip() {
            call(400); call(404); call(422);
            assertEquals(CircuitBreaker.State.CLOSED, state());
        }

        @Test @DisplayName("network exception counts as failure")
        void networkException_countsAsFailure() {
            WebClient throwingClient = new WebClient(
                req -> { throw new RuntimeException("connection refused"); },
                clock);
            throwingClient.registerConfig(SVC_B, new CircuitBreakerConfig(
                3, 10 * 60_000L, 5 * 60_000L));

            for (int i = 0; i < 3; i++) {
                try { throwingClient.execute(new Request(SVC_B, "/")); }
                catch (Exception ignored) { }
            }
            assertEquals(CircuitBreaker.State.OPEN, throwingClient.stateFor(SVC_B));
        }

        @Test @DisplayName("unknown service gets DEFAULT_CONFIG")
        void unknownService_usesDefault() {
            statusCode.set(200);
            assertDoesNotThrow(
                () -> client.execute(new Request("SVC_X", "/test")));
            assertEquals(CircuitBreaker.State.CLOSED, client.stateFor("SVC_X"));
        }

        @Test @DisplayName("invalid config throws at construction")
        void invalidConfig_throws() {
            assertThrows(IllegalArgumentException.class,
                () -> new CircuitBreakerConfig(0, 60_000L, 60_000L));
            assertThrows(IllegalArgumentException.class,
                () -> new CircuitBreakerConfig(3, -1L, 60_000L));
        }
    }

    // ─────────────────────────────────────────────────────────
    // PER-SERVICE CONFIG
    // ─────────────────────────────────────────────────────────

    @Nested @DisplayName("Per-service config")
    class PerServiceConfig {

        @Test @DisplayName("SVC_B (threshold=3) trips, SVC_C (threshold=10) does not")
        void differentThresholds() {
            WebClient w = new WebClient(
                req -> new Response(statusCode.get(), "body"), clock);
            w.registerConfig("SVC_B", new CircuitBreakerConfig(3,  10 * 60_000L, 5 * 60_000L));
            w.registerConfig("SVC_C", new CircuitBreakerConfig(10, 10 * 60_000L, 5 * 60_000L));

            statusCode.set(500);
            for (int i = 0; i < 3; i++) {
                try { w.execute(new Request("SVC_B", "/")); } catch (Exception ignored) {}
                try { w.execute(new Request("SVC_C", "/")); } catch (Exception ignored) {}
            }

            assertEquals(CircuitBreaker.State.OPEN,   w.stateFor("SVC_B"));
            assertEquals(CircuitBreaker.State.CLOSED, w.stateFor("SVC_C"));
        }
    }

    // ─────────────────────────────────────────────────────────
    // CONCURRENCY
    // ─────────────────────────────────────────────────────────

    @Nested @DisplayName("Concurrency")
    class Concurrency {

        @Test @DisplayName("concurrent failures — circuit trips exactly once")
        void concurrentFailures_tripOnce() throws InterruptedException {
            int            threads = 20;
            CountDownLatch start   = new CountDownLatch(1);
            CountDownLatch done    = new CountDownLatch(threads);

            statusCode.set(500);
            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    try {
                        start.await();
                        client.execute(new Request(SVC_B, "/test"));
                    } catch (Exception ignored) {
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();
            done.await(5, TimeUnit.SECONDS);

            // Must be OPEN — not CLOSED due to a lost update
            assertEquals(CircuitBreaker.State.OPEN, state());
        }
    }
}
