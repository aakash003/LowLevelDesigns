package org.flipkart.circuitbreaker;

import org.junit.jupiter.api.*;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full behavioural test suite for WebClient + CircuitBreaker.
 *
 * All time-based transitions use TestClock.advance() — no Thread.sleep().
 *
 * Coverage:
 *   ClosedState      → normal operation, failure counting, boundaries
 *   OpenState        → blocking, cooldown, service isolation
 *   HalfOpenState    → probe mechanics, recovery, re-trip
 *   SlidingWindow    → stale eviction, boundary cases
 *   EdgeCases        → 4xx, network exceptions, unknown services, concurrency
 *   PerServiceConfig → each service gets its own config
 */
class WebClientTest {

    private static final String SVC_B = "SVC_B";
    private static final String SVC_C = "SVC_C";

    private TestClock       clock;
    private AtomicInteger   statusCode;
    private WebClient       client;

    @BeforeEach
    void setUp() {
        clock      = new TestClock(Instant.EPOCH);
        statusCode = new AtomicInteger(200);
        client     = new WebClient(
            req -> new Response(statusCode.get(), "body"),
            clock
        );
        client.registerConfig(SVC_B, CircuitBreakerConfig.builder()
            .failureThreshold(3)
            .windowMinutes(10)
            .cooldownMinutes(5)
            .build());
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private void call(String svc, int status) {
        statusCode.set(status);
        try { client.execute(new Request(svc, "/test")); }
        catch (ServiceUnavailableException ignored) { }
    }

    private void call(int status) { call(SVC_B, status); }

    private void assertBlocked() {
        statusCode.set(200);
        assertThrows(ServiceUnavailableException.class,
            () -> client.execute(new Request(SVC_B, "/test")));
    }

    private void assertAllowed() {
        statusCode.set(200);
        assertDoesNotThrow(
            () -> client.execute(new Request(SVC_B, "/test")));
    }

    private CircuitBreakerState.State state(String svc) {
        return client.stateFor(svc);
    }
    private CircuitBreakerState.State state() { return state(SVC_B); }

    private void tripBreaker() {
        call(500); call(500); call(500);
        assertEquals(CircuitBreakerState.State.OPEN, state(),
            "Precondition: breaker should be OPEN");
    }

    // ─────────────────────────────────────────────────────────────────────
    // CLOSED STATE
    // ─────────────────────────────────────────────────────────────────────

    @Nested @DisplayName("CLOSED state")
    class ClosedState {

        @Test @DisplayName("starts CLOSED")
        void startsClosed() {
            assertEquals(CircuitBreakerState.State.CLOSED, state());
        }

        @Test @DisplayName("allows requests with no failures")
        void allowsRequests_noFailures() {
            assertAllowed();
            assertEquals(CircuitBreakerState.State.CLOSED, state());
        }

        @Test @DisplayName("two failures do not trip")
        void twoFailures_doNotTrip() {
            call(500); call(500);
            assertEquals(CircuitBreakerState.State.CLOSED, state());
            assertAllowed();
        }

        @Test @DisplayName("exactly three failures trip — boundary check")
        void exactlyThreeFailures_tripsBreaker() {
            call(500); call(500);
            assertEquals(CircuitBreakerState.State.CLOSED, state()); // 2 — not yet

            call(500);
            assertEquals(CircuitBreakerState.State.OPEN, state());   // 3 — tripped
        }

        @Test @DisplayName("success does not reset failure window")
        void success_doesNotResetWindow() {
            // Intentional: success shouldn't protect against a burst
            call(500); call(500);
            call(200);   // success — window NOT cleared
            call(500);   // 3rd failure total — should trip
            assertEquals(CircuitBreakerState.State.OPEN, state());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // OPEN STATE
    // ─────────────────────────────────────────────────────────────────────

    @Nested @DisplayName("OPEN state")
    class OpenState {

        @Test @DisplayName("blocks all requests when OPEN")
        void blocksRequests_whenOpen() {
            tripBreaker();
            assertBlocked();
            assertBlocked();
            assertBlocked();
        }

        @Test @DisplayName("does NOT transition before cooldown")
        void doesNotTransition_beforeCooldown() {
            tripBreaker();
            clock.advance(4 * 60_000L);   // 4 min — need 5
            assertBlocked();
            assertEquals(CircuitBreakerState.State.OPEN, state());
        }

        @Test @DisplayName("exception carries state and service info")
        void exception_carriesMetadata() {
            tripBreaker();
            ServiceUnavailableException ex = assertThrows(
                ServiceUnavailableException.class,
                () -> client.execute(new Request(SVC_B, "/test")));

            assertEquals(SVC_B, ex.getService());
            assertEquals(CircuitBreakerState.State.OPEN, ex.getCircuitState());
        }

        @Test @DisplayName("SVC_B trip does NOT affect SVC_C — service isolation")
        void serviceIsolation() {
            tripBreaker();
            assertEquals(CircuitBreakerState.State.OPEN,   state(SVC_B));
            assertEquals(CircuitBreakerState.State.CLOSED, state(SVC_C));

            statusCode.set(200);
            assertDoesNotThrow(
                () -> client.execute(new Request(SVC_C, "/test")));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // HALF_OPEN STATE
    // ─────────────────────────────────────────────────────────────────────

    @Nested @DisplayName("HALF_OPEN state")
    class HalfOpenState {

        @BeforeEach
        void tripAndWait() {
            tripBreaker();
            clock.advance(5 * 60_000L + 1);   // just past cooldown
        }

        @Test @DisplayName("transitions to HALF_OPEN after cooldown")
        void transitionsToHalfOpen() {
            assertEquals(CircuitBreakerState.State.HALF_OPEN, state());
        }

        @Test @DisplayName("allows exactly one probe in HALF_OPEN")
        void allowsOnlyOneProbe() {
            statusCode.set(200);
            // First request claims probe slot
            assertDoesNotThrow(
                () -> client.execute(new Request(SVC_B, "/test")));
            // Second request rejected while probe in flight
            assertBlocked();
        }

        @Test @DisplayName("successful probe → CLOSED")
        void successfulProbe_closesBraker() {
            statusCode.set(200);
            assertDoesNotThrow(
                () -> client.execute(new Request(SVC_B, "/test")));

            assertEquals(CircuitBreakerState.State.CLOSED, state());
            assertAllowed();   // subsequent calls go through normally
        }

        @Test @DisplayName("failed probe → OPEN with fresh cooldown")
        void failedProbe_reOpensBraker() {
            call(500);   // probe fails
            assertEquals(CircuitBreakerState.State.OPEN, state());

            // Still within new cooldown — must be blocked
            assertBlocked();

            // Advance past new cooldown → HALF_OPEN again
            clock.advance(5 * 60_000L + 1);
            assertEquals(CircuitBreakerState.State.HALF_OPEN, state());
        }

        @Test @DisplayName("HALF_OPEN exception carries correct state")
        void halfOpenException_carriesState() {
            // Claim probe slot first
            statusCode.set(200);
            try { client.execute(new Request(SVC_B, "/test")); }
            catch (Exception ignored) { }

            // Second request — probe in flight
            ServiceUnavailableException ex = assertThrows(
                ServiceUnavailableException.class,
                () -> client.execute(new Request(SVC_B, "/test")));
            assertEquals(CircuitBreakerState.State.HALF_OPEN, ex.getCircuitState());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // SLIDING WINDOW
    // ─────────────────────────────────────────────────────────────────────

    @Nested @DisplayName("Sliding window")
    class SlidingWindow {

        @Test @DisplayName("failures outside 10-min window evicted — does NOT trip")
        void staleFailures_evicted() {
            call(500); call(500);              // T+0: fail 1, 2
            clock.advance(11 * 60_000L);       // past window
            call(500);                         // T+11m: only 1 fresh failure
            assertEquals(CircuitBreakerState.State.CLOSED, state());
        }

        @Test @DisplayName("failure at exact boundary is evicted — boundary check")
        void failureAtExactBoundary_evicted() {
            // window = 10 min
            call(500);                         // T+0: fail 1
            call(500);                         // T+0: fail 2
            clock.advance(10 * 60_000L);       // advance to exactly T+10m
            call(500);                         // T+10m: fail 3

            // evict: cutoff = T+10m - 10m = T+0
            // peekFirst = T+0 <= T+0 → evicted (both T+0 failures evicted)
            // window = [fail3@T+10m], size=1 < 3 → NOT tripped
            assertEquals(CircuitBreakerState.State.CLOSED, state());
        }

        @Test @DisplayName("three fresh failures spread across window DOES trip")
        void threeFreshFailures_withinWindow_trips() {
            call(500);                         // T+0
            clock.advance(4 * 60_000L);
            call(500);                         // T+4
            clock.advance(4 * 60_000L);
            call(500);                         // T+8 — all within 10 min
            assertEquals(CircuitBreakerState.State.OPEN, state());
        }

        @Test @DisplayName("evict BEFORE add — fix for original ordering bug")
        void evictBeforeAdd_correctOrdering() {
            // If evict happened AFTER add, the new failure would be in the
            // deque when we check size — potentially tripping on stale count.
            call(500);                         // T+0  fail 1
            call(500);                         // T+0  fail 2
            clock.advance(10 * 60_000L + 1);   // T+10m+1ms — both now stale
            call(500);                         // T+10m+1: only 1 fresh failure after evict
            assertEquals(CircuitBreakerState.State.CLOSED, state());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // EDGE CASES
    // ─────────────────────────────────────────────────────────────────────

    @Nested @DisplayName("Edge cases")
    class EdgeCases {

        @Test @DisplayName("4xx does NOT trip breaker — client error not server fault")
        void clientError_doesNotTrip() {
            call(400); call(404); call(422);
            assertEquals(CircuitBreakerState.State.CLOSED, state());
        }

        @Test @DisplayName("network exception counts as failure")
        void networkException_countsAsFailure() {
            WebClient throwingClient = new WebClient(
                req -> { throw new RuntimeException("connection refused"); },
                clock
            );
            throwingClient.registerConfig(SVC_B, CircuitBreakerConfig.builder()
                .failureThreshold(3).windowMinutes(10).cooldownMinutes(5).build());

            for (int i = 0; i < 3; i++) {
                try { throwingClient.execute(new Request(SVC_B, "/")); }
                catch (Exception ignored) { }
            }
            assertEquals(CircuitBreakerState.State.OPEN, throwingClient.stateFor(SVC_B));
        }

        @Test @DisplayName("unknown service gets DEFAULT_CONFIG automatically")
        void unknownService_usesDefaultConfig() {
            // SVC_X not registered — should start CLOSED with default config
            statusCode.set(200);
            assertDoesNotThrow(
                () -> client.execute(new Request("SVC_X", "/test")));
            assertEquals(CircuitBreakerState.State.CLOSED, client.stateFor("SVC_X"));
        }

        @Test @DisplayName("invalid config throws at construction")
        void invalidConfig_throwsAtConstruction() {
            assertThrows(IllegalArgumentException.class,
                () -> new CircuitBreakerConfig(0, 60_000L, 60_000L));
            assertThrows(IllegalArgumentException.class,
                () -> new CircuitBreakerConfig(3, -1L, 60_000L));
            assertThrows(IllegalArgumentException.class,
                () -> new CircuitBreakerConfig(3, 60_000L, 0L));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // PER-SERVICE CONFIG
    // ─────────────────────────────────────────────────────────────────────

    @Nested @DisplayName("Per-service config")
    class PerServiceConfig {

        @Test @DisplayName("SVC_B (threshold=3) trips faster than SVC_C (threshold=10)")
        void differentThresholds_independentBehavior() {
            WebClient multiClient = new WebClient(
                req -> new Response(statusCode.get(), "body"), clock);

            multiClient.registerConfig("SVC_B", CircuitBreakerConfig.builder()
                .failureThreshold(3).windowMinutes(10).cooldownMinutes(5).build());
            multiClient.registerConfig("SVC_C", CircuitBreakerConfig.builder()
                .failureThreshold(10).windowMinutes(5).cooldownMinutes(2).build());

            statusCode.set(500);

            // Trip SVC_B with 3 failures
            for (int i = 0; i < 3; i++) {
                try { multiClient.execute(new Request("SVC_B", "/")); }
                catch (Exception ignored) { }
            }
            assertEquals(CircuitBreakerState.State.OPEN, multiClient.stateFor("SVC_B"));

            // SVC_C needs 10 — 3 failures should NOT trip it
            for (int i = 0; i < 3; i++) {
                try { multiClient.execute(new Request("SVC_C", "/")); }
                catch (Exception ignored) { }
            }
            assertEquals(CircuitBreakerState.State.CLOSED, multiClient.stateFor("SVC_C"));
        }

        @Test @DisplayName("SVC_C has shorter cooldown than SVC_B")
        void differentCooldowns() {
            WebClient multiClient = new WebClient(
                req -> new Response(statusCode.get(), "body"), clock);

            multiClient.registerConfig("SVC_B", CircuitBreakerConfig.builder()
                .failureThreshold(3).windowMinutes(10).cooldownMinutes(5).build());
            multiClient.registerConfig("SVC_C", CircuitBreakerConfig.builder()
                .failureThreshold(3).windowMinutes(10).cooldownMinutes(2).build());

            statusCode.set(500);
            // Trip both
            for (int i = 0; i < 3; i++) {
                try { multiClient.execute(new Request("SVC_B", "/")); } catch (Exception ignored) {}
                try { multiClient.execute(new Request("SVC_C", "/")); } catch (Exception ignored) {}
            }

            // Advance 3 min — SVC_C cooldown (2min) done, SVC_B (5min) not
            clock.advance(3 * 60_000L);
            assertEquals(CircuitBreakerState.State.OPEN,     multiClient.stateFor("SVC_B"));
            assertEquals(CircuitBreakerState.State.HALF_OPEN, multiClient.stateFor("SVC_C"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // CONCURRENCY
    // ─────────────────────────────────────────────────────────────────────

    @Nested @DisplayName("Concurrency")
    class ConcurrencyTests {

        @Test @DisplayName("only one probe allowed — concurrent threads in HALF_OPEN")
        void onlyOneProbe_concurrentThreads() throws InterruptedException {
            tripBreaker();
            clock.advance(5 * 60_000L + 1);

            int              threads = 20;
            CountDownLatch   start   = new CountDownLatch(1);
            CountDownLatch   done    = new CountDownLatch(threads);
            AtomicInteger    probes  = new AtomicInteger(0);
            AtomicInteger    blocked = new AtomicInteger(0);

            statusCode.set(200);

            for (int i = 0; i < threads; i++) {
                new Thread(() -> {
                    try {
                        start.await();
                        client.execute(new Request(SVC_B, "/test"));
                        probes.incrementAndGet();
                    } catch (ServiceUnavailableException e) {
                        blocked.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }).start();
            }

            start.countDown();   // all threads start simultaneously
            done.await(5, TimeUnit.SECONDS);

            // Exactly one probe should have gotten through
            assertEquals(1, probes.get(),
                "Exactly one probe should be allowed in HALF_OPEN");
            assertEquals(threads - 1, blocked.get(),
                "All other threads should be blocked");
        }

        @Test @DisplayName("concurrent failures — exactly one trips the circuit")
        void concurrentFailures_exactlyOneTrips() throws InterruptedException {
            int            threads = 10;
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

            // State should be OPEN — not CLOSED due to lost update
            assertEquals(CircuitBreakerState.State.OPEN, state());
        }
    }
}
