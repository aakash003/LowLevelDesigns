package org.flipkart.circuitbreaker;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Per-service circuit breaker — OPEN and CLOSED only (no HALF_OPEN).
 *
 * State machine:
 *   CLOSED ──(Y failures in last X min)──► OPEN
 *   OPEN   ──(Z min cooldown elapsed)  ──► CLOSED
 *
 * Thread-safety model:
 *   ReadWriteLock → many threads read state concurrently (CLOSED fast path)
 *                   one thread at a time writes (state transitions)
 *   volatile      → state + openedAt visible across threads immediately
 *   Clock         → injectable so tests advance time without Thread.sleep()
 *
 * Three-layer trip guard in onFailure():
 *   Layer 1 — evictStaleFailures : removes failures outside X-min window
 *   Layer 2 — size >= threshold  : count check
 *   Layer 3 — isWithinWindow     : time span guard (defence in depth)
 */
public class CircuitBreaker {

    enum State { CLOSED, OPEN }

    // ── config ────────────────────────────────────────────────────────────
    private final int   failureThreshold;
    private final long  windowMillis;
    private final long  cooldownMillis;
    private final Clock clock;

    // ── state ─────────────────────────────────────────────────────────────
    private volatile State   state    = State.CLOSED;
    private volatile Instant openedAt = null;

    /** Sliding window: epoch-ms timestamps of recent failures. */
    private final Deque<Long> failureTimes = new ArrayDeque<>();

    /** Guards all state + failureTimes mutations. */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // ── constructors ──────────────────────────────────────────────────────

    /** Production — system UTC clock. */
    public CircuitBreaker(int failureThreshold, long windowMillis, long cooldownMillis) {
        this(failureThreshold, windowMillis, cooldownMillis, Clock.systemUTC());
    }

    /** Tests — injectable clock, no Thread.sleep() needed. */
    public CircuitBreaker(int failureThreshold, long windowMillis,
                          long cooldownMillis, Clock clock) {
        this.failureThreshold = failureThreshold;
        this.windowMillis     = windowMillis;
        this.cooldownMillis   = cooldownMillis;
        this.clock            = clock;
    }

    // ── public API ────────────────────────────────────────────────────────

    /**
     * Gate check — call BEFORE making the downstream request.
     *
     * Two-phase lock pattern (read → optional write) to avoid
     * the ReentrantReadWriteLock double-unlock bug.
     *
     * @throws CircuitOpenException if OPEN and cooldown not yet elapsed.
     */
    public void preRequest() {

        // Phase 1: snapshot state under read lock
        final State snapshot;
        lock.readLock().lock();
        try {
            snapshot = state;
        } finally {
            lock.readLock().unlock();
        }

        // Phase 2: if OPEN, check cooldown under write lock
        if (snapshot == State.OPEN) {
            lock.writeLock().lock();
            try {
                if (state == State.OPEN) {                       // double-check
                    long nowMs   = clock.instant().toEpochMilli();
                    long elapsed = nowMs - openedAt.toEpochMilli();

                    if (elapsed >= cooldownMillis) {
                        failureTimes.clear();
                        openedAt = null;
                        transitionTo(State.CLOSED);
                    } else {
                        long remainSec = (cooldownMillis - elapsed) / 1_000;
                        throw new CircuitOpenException(
                            "Circuit OPEN for '" + "'. Retry in " + remainSec + "s.");
                    }
                }
                // Another thread already closed it — fall through.
            } finally {
                lock.writeLock().unlock();
            }
        }
        // CLOSED → allow request.
    }

    /** Call after a successful response. */
    public void onSuccess() {
        // Intentionally no window reset on success —
        // a burst of failures can still trip even with interleaved successes.
    }

    /**
     * Call after a failed response (5xx, timeout, network error).
     *
     * Three-layer trip guard:
     *   1. evictStaleFailures → clean the sliding window
     *   2. size >= threshold  → count check
     *   3. isWithinWindow     → time span guard (defence in depth)
     */
    public void onFailure() {
        long nowMs = clock.instant().toEpochMilli();
        lock.writeLock().lock();
        try {
            if (state == State.OPEN) return;         // already open, ignore

            // Layer 1: evict failures outside the X-min window
            evictStaleFailures(nowMs);

            // Record this failure
            failureTimes.addLast(nowMs);

            // Layer 2 + 3: count check AND time span check
            if (failureTimes.size() >= failureThreshold
                    && isWithinWindow(nowMs)) {
                openedAt = Instant.ofEpochMilli(nowMs);
                transitionTo(State.OPEN);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Observable state — for metrics / health endpoints.
     * Promotes OPEN → CLOSED automatically if cooldown elapsed.
     */
    public State getState() {
        // Fast path under read lock
        lock.readLock().lock();
        try {
            if (state != State.OPEN) return state;
            long elapsed = clock.instant().toEpochMilli() - openedAt.toEpochMilli();
            if (elapsed < cooldownMillis) return State.OPEN;
        } finally {
            lock.readLock().unlock();
        }

        // Cooldown elapsed — promote under write lock
        lock.writeLock().lock();
        try {
            if (state == State.OPEN) {                           // double-check
                long elapsed = clock.instant().toEpochMilli() - openedAt.toEpochMilli();
                if (elapsed >= cooldownMillis) {
                    failureTimes.clear();
                    openedAt = null;
                    transitionTo(State.CLOSED);
                }
            }
            return state;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ── private helpers ───────────────────────────────────────────────────

    /** Layer 1: remove failures older than X minutes. Called under write lock. */
    private void evictStaleFailures(long nowMs) {
        long cutoff = nowMs - windowMillis;
        while (!failureTimes.isEmpty() && failureTimes.peekFirst() <= cutoff) {
            failureTimes.pollFirst();
        }
    }

    /**
     * Layer 3: confirms the oldest failure in the deque is within the window.
     * Defence in depth against clock skew or edge cases in eviction.
     * Called under write lock.
     */
    private boolean isWithinWindow(long nowMs) {
        if (failureTimes.isEmpty()) return false;
        long oldestMs = failureTimes.peekFirst();
        return (nowMs - oldestMs) <= windowMillis;
    }

    private void transitionTo(State next) {
        System.out.printf("  [CB] %s → %s%n", state, next);
        state = next;
    }
}
