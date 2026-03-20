package org.flipkart.circuitbreaker;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Per-service circuit breaker (state machine: CLOSED → OPEN → HALF_OPEN → CLOSED).
 *
 * Thread-safe: ReadWriteLock guards state transitions; AtomicBoolean gates the
 * single half-open trial request.
 */
public class CircuitBreaker {

    // ── configuration ────────────────────────────────────────────────────────
    private final int    failureThreshold;    // failures before tripping
    private final long   windowMillis;        // sliding window width
    private final long   cooldownMillis;      // how long to stay OPEN

    // ── sliding failure window ────────────────────────────────────────────────
    /** Timestamps (epoch ms) of recent failures still inside the window. */
    private final Deque<Long> failureTimes = new ArrayDeque<>();

    // ── circuit state ─────────────────────────────────────────────────────────
    enum State { CLOSED, OPEN, HALF_OPEN }

    private State   state          = State.CLOSED;
    private Instant openedAt       = null;   // when the breaker last tripped

    /** Guards all state + failureTimes mutations. */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * One-at-a-time gate for the half-open trial.
     * true  → a trial is already in flight; all other threads are rejected.
     * false → the next thread may take the trial slot.
     */
    private final AtomicBoolean trialInFlight = new AtomicBoolean(false);

    // ── constructor ──────────────────────────────────────────────────────────
    public CircuitBreaker(int failureThreshold, long windowMillis, long cooldownMillis) {
        this.failureThreshold = failureThreshold;
        this.windowMillis     = windowMillis;
        this.cooldownMillis   = cooldownMillis;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Call before sending a request to the downstream service.
     *
     * @throws CircuitOpenException if the breaker is OPEN (or HALF_OPEN
     *                              and a trial is already in flight).
     */
    public void preRequest() {
        lock.readLock().lock();
        try {
            State current = resolveState();          // may be OPEN or HALF_OPEN
            if (current == State.CLOSED) return;     // fast path — no contention

            if (current == State.OPEN) {
                throw new CircuitOpenException("Circuit is OPEN. Requests are blocked.");
            }

            // HALF_OPEN: only one trial at a time
            if (!trialInFlight.compareAndSet(false, true)) {
                throw new CircuitOpenException(
                    "Circuit is HALF_OPEN and a trial request is already in flight.");
            }
            // this thread claimed the trial slot — let it proceed
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Call after a successful response.
     * Resets the breaker to CLOSED and clears the failure window.
     */
    public void onSuccess() {
        lock.writeLock().lock();
        try {
            state = State.CLOSED;
            failureTimes.clear();
            openedAt = null;
        } finally {
            lock.writeLock().unlock();
            trialInFlight.set(false);   // release trial gate outside write-lock
        }
    }

    /**
     * Call after a failed response (non-2xx, timeout, connection error …).
     * Records the failure and trips the breaker if the threshold is exceeded.
     */
    public void onFailure() {
        long now = Instant.now().toEpochMilli();
        lock.writeLock().lock();
        try {
            evictOldFailures(now);
            failureTimes.addLast(now);

            if (failureTimes.size() >= failureThreshold) {
                trip(now);
            }
        } finally {
            lock.writeLock().unlock();
            trialInFlight.set(false);   // release trial gate outside write-lock
        }
    }

    /** Current observable state (for metrics / health endpoints). */
    public State getState() {
        lock.readLock().lock();
        try {
            return resolveState();
        } finally {
            lock.readLock().unlock();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers  (always called while holding at least the read lock)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Compute the *effective* state, automatically transitioning OPEN → HALF_OPEN
     * once the cooldown period has elapsed.
     *
     * Callers hold the read lock; if a state upgrade is needed we upgrade to the
     * write lock (try-lock style to avoid deadlock with a double read-then-write).
     */
    private State resolveState() {
        if (state != State.OPEN) return state;

        long elapsed = Instant.now().toEpochMilli() - openedAt.toEpochMilli();
        if (elapsed >= cooldownMillis) {
            // Upgrade to write lock to transition state safely.
            // We must release the read lock first (ReentrantReadWriteLock rule).
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                // Re-check: another thread may have already transitioned.
                if (state == State.OPEN) {
                    long elapsedNow = Instant.now().toEpochMilli() - openedAt.toEpochMilli();
                    if (elapsedNow >= cooldownMillis) {
                        state = State.HALF_OPEN;
                        trialInFlight.set(false);
                    }
                }
                return state;
            } finally {
                // Downgrade back to read lock before returning.
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
        return State.OPEN;
    }

    /** Remove failure timestamps that have slid outside the window. */
    private void evictOldFailures(long nowMs) {
        long cutoff = nowMs - windowMillis;
        while (!failureTimes.isEmpty() && failureTimes.peekFirst() <= cutoff) {
            failureTimes.pollFirst();
        }
    }

    /** Trip the breaker to OPEN state. */
    private void trip(long nowMs) {
        state    = State.OPEN;
        openedAt = Instant.ofEpochMilli(nowMs);
    }
}
