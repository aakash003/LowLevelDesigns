package org.flipkart.circuitbreaker.service;



import org.flipkart.circuitbreaker.exceptions.CircuitOpenException;
import org.flipkart.circuitbreaker.model.enums.State;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Per-service circuit breaker (state machine: CLOSED → OPEN → HALF_OPEN → CLOSED).
 *
 * Thread-safety model:
 *   - ReadWriteLock  : guards all state + failureTimes mutations.
 *   - AtomicBoolean  : gates the single half-open trial without holding a lock.
 *   - Clock          : injected so tests can advance time deterministically.
 *
 * Lock discipline (critical — do NOT violate):
 *   preRequest() acquires/releases the read lock in two SEPARATE try-finally
 *   blocks with an optional write-lock section between them.
 *   This avoids the double-unlock bug that arises when resolveState() is called
 *   while the caller already holds the read lock — ReentrantReadWriteLock does
 *   NOT support lock promotion (read → write without releasing first).
 *
 *   resolveState() pattern is REMOVED from preRequest() entirely.
 *   getState() (metrics/health) does its own clean two-phase read→write upgrade.
 */
public class CircuitBreaker {

    // ── configuration ────────────────────────────────────────────────────────
    private final int   failureThreshold;   // failures before tripping
    private final long  windowMillis;       // sliding window width
    private final long  cooldownMillis;     // how long to stay OPEN

    // ── injectable clock ─────────────────────────────────────────────────────
    // Swap for a TestClock in unit tests to advance time without Thread.sleep().
    private final Clock clock;

    // ── sliding failure window ────────────────────────────────────────────────
    /** Epoch-ms timestamps of recent failures still inside the window. */
    private final Deque<Long> failureTimes = new ArrayDeque<>();

    // ── circuit state ─────────────────────────────────────────────────────────


    private volatile State state  = State.CLOSED;
    private volatile Instant openedAt = null;

    /** Guards all state + failureTimes mutations. */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Half-open trial gate.
     *   false → next caller may claim the trial slot.
     *   true  → trial in flight; all other callers are rejected.
     */
    private final AtomicBoolean trialInFlight = new AtomicBoolean(false);

    // ── constructors ─────────────────────────────────────────────────────────

    /** Full constructor — used in production AND tests. */
    public CircuitBreaker(int failureThreshold, long windowMillis,
                          long cooldownMillis, Clock clock) {
        this.failureThreshold = failureThreshold;
        this.windowMillis     = windowMillis;
        this.cooldownMillis   = cooldownMillis;
        this.clock            = clock;
    }

    /** Convenience constructor for production code — uses the system UTC clock. */
    public CircuitBreaker(int failureThreshold, long windowMillis, long cooldownMillis) {
        this(failureThreshold, windowMillis, cooldownMillis, Clock.systemUTC());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gate check — call BEFORE sending a request to the downstream service.
     *
     * Uses two separate read-lock sections with an optional write-lock between
     * them (snapshot → maybe-transition → re-check) to avoid the
     * ReentrantReadWriteLock double-unlock bug.
     *
     * @throws CircuitOpenException if the circuit is OPEN (cooldown not elapsed)
     *                              or HALF_OPEN with a trial already in flight.
     */
    public void preRequest() {

        // ── Phase 1: snapshot current state ──────────────────────────────
        final State snapshot;
        lock.readLock().lock();
        try {
            snapshot = state;
        } finally {
            lock.readLock().unlock();
        }

        // ── Phase 2: OPEN → HALF_OPEN transition under write lock ────────
        if (snapshot == State.OPEN) {
            lock.writeLock().lock();
            try {
                // Double-check: another thread may have already transitioned.
                if (state == State.OPEN) {
                    long nowMs   = clock.instant().toEpochMilli();
                    long elapsed = nowMs - openedAt.toEpochMilli();

                    if (elapsed >= cooldownMillis) {
                        state = State.HALF_OPEN;
                        trialInFlight.set(false);
                    } else {
                        long remainingMs = cooldownMillis - elapsed;
                        throw new CircuitOpenException(
                            "Circuit is OPEN. Retry in " + (remainingMs / 1000) + "s.");
                    }
                }
                // If another thread already transitioned to HALF_OPEN, fall through.
            } finally {
                lock.writeLock().unlock();
            }
        }

        // ── Phase 3: re-read state; enforce HALF_OPEN one-at-a-time gate ─
        lock.readLock().lock();
        try {
            if (state == State.OPEN) {
                // Race: another thread re-opened while we were in phase 2.
                throw new CircuitOpenException("Circuit is OPEN. Requests blocked.");
            }
            if (state == State.HALF_OPEN) {
                if (!trialInFlight.compareAndSet(false, true)) {
                    throw new CircuitOpenException(
                        "Circuit is HALF_OPEN and a trial is already in flight.");
                }
            }
            // CLOSED → fall through, request is allowed.
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Call after a SUCCESSFUL response.
     * Transitions to CLOSED and clears the failure window.
     */
    public void onSuccess() {
        lock.writeLock().lock();
        try {
            state    = State.CLOSED;
            openedAt = null;
            failureTimes.clear();
        } finally {
            lock.writeLock().unlock();
        }
        // Release trial gate OUTSIDE the write lock to minimise lock hold time.
        trialInFlight.set(false);
    }

    /**
     * Call after a FAILED response (5xx, timeout, connection error, …).
     *
     * HALF_OPEN failure → immediately re-open and reset the cooldown timer.
     *   We do NOT add to the sliding window — one failed probe is enough.
     *
     * CLOSED failure    → record in the sliding window; trip to OPEN if the
     *   window now holds ≥ failureThreshold entries.
     */
    public void onFailure() {
        long nowMs = clock.instant().toEpochMilli();
        lock.writeLock().lock();
        try {
            if (state == State.HALF_OPEN) {
                // Probe failed → re-open immediately with a fresh cooldown timer.
                trip(nowMs);
                return;
            }

            if (state == State.CLOSED) {
                evictOldFailures(nowMs);
                failureTimes.addLast(nowMs);
                if (failureTimes.size() >= failureThreshold) {
                    trip(nowMs);
                }
            }
            // OPEN → concurrent stragglers; ignore safely.
        } finally {
            lock.writeLock().unlock();
        }
        // Release trial gate OUTSIDE the write lock.
        trialInFlight.set(false);
    }

    /**
     * Returns the effective current state (for metrics / health endpoints).
     * Automatically promotes OPEN → HALF_OPEN if the cooldown has elapsed,
     * using a clean read → write two-phase upgrade (no lock held across both).
     */
    public State getState() {
        // Fast path under read lock.
        lock.readLock().lock();
        try {
            if (state != State.OPEN) return state;
            long elapsed = clock.instant().toEpochMilli() - openedAt.toEpochMilli();
            if (elapsed < cooldownMillis) return State.OPEN;
        } finally {
            lock.readLock().unlock();
        }

        // Cooldown elapsed — promote to HALF_OPEN under write lock.
        lock.writeLock().lock();
        try {
            // Double-check inside write lock.
            if (state == State.OPEN) {
                long elapsed = clock.instant().toEpochMilli() - openedAt.toEpochMilli();
                if (elapsed >= cooldownMillis) {
                    state = State.HALF_OPEN;
                    trialInFlight.set(false);
                }
            }
            return state;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Drop failure timestamps that have slid outside the window. */
    private void evictOldFailures(long nowMs) {
        long cutoff = nowMs - windowMillis;
        while (!failureTimes.isEmpty() && failureTimes.peekFirst() <= cutoff) {
            failureTimes.pollFirst();
        }
    }

    /** Transition to OPEN and stamp when we tripped. */
    private void trip(long nowMs) {
        state    = State.OPEN;
        openedAt = Instant.ofEpochMilli(nowMs);
    }
}
