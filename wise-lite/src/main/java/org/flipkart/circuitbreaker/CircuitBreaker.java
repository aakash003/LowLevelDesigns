package org.flipkart.circuitbreaker;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Per-service circuit breaker — OPEN and CLOSED only.
 *
 * ── State machine ────────────────────────────────────────────
 *
 *   CLOSED ──(Y failures in last X min)──► OPEN
 *   OPEN   ──(Z min cooldown elapsed)   ──► CLOSED
 *
 * ── Thread-safety model ──────────────────────────────────────
 *
 *   ReadWriteLock:
 *     read  lock → allowRequest() in CLOSED (hot path, many threads)
 *     write lock → recordFailure(), state transitions (one thread)
 *
 *   Two-phase pattern in allowRequest():
 *     Phase 1: snapshot state under read lock
 *     Phase 2: transition under write lock if OPEN
 *     Double-check inside write lock — another thread may have
 *     already transitioned while we were waiting.
 *
 *   volatile on state + openedAtMs:
 *     Visibility guarantee for reads before lock acquisition.
 *
 * ── Three-layer trip guard in recordFailure() ────────────────
 *
 *   Layer 1: evictStale BEFORE addLast   (order matters!)
 *   Layer 2: size >= threshold           (count check)
 *   Layer 3: isWithinWindow              (time span guard)
 *
 * ── Clock injection ──────────────────────────────────────────
 *
 *   Swap TestClock in tests — no Thread.sleep() needed.
 */
public class CircuitBreaker {

    enum State { CLOSED, OPEN }

    // ── config ────────────────────────────────────────────────
    private final int   failureThreshold;
    private final long  windowMs;
    private final long  cooldownMs;
    private final Clock clock;

    // ── mutable state — guarded by rwLock ────────────────────
    private volatile State state      = State.CLOSED;
    private volatile long  openedAtMs = 0L;

    // Sliding window of failure timestamps (epoch ms).
    // ArrayDeque: O(1) addLast + O(1) pollFirst, cache-friendly.
    private final Deque<Long> failureTimes = new ArrayDeque<>();

    // ── lock ─────────────────────────────────────────────────
    private final ReentrantReadWriteLock rwLock
        = new ReentrantReadWriteLock();

    // ── constructors ──────────────────────────────────────────

    /** Production — system clock. */
    public CircuitBreaker(CircuitBreakerConfig config) {
        this(config, Clock.systemUTC());
    }

    /** Tests — injectable clock. */
    public CircuitBreaker(CircuitBreakerConfig config, Clock clock) {
        this.failureThreshold = config.failureThreshold;
        this.windowMs         = config.windowMs;
        this.cooldownMs       = config.cooldownMs;
        this.clock            = clock;
    }

    // ── public API ────────────────────────────────────────────

    /**
     * Gate check — call BEFORE the downstream request.
     *
     * Two-phase lock pattern:
     *   Phase 1: read lock  → CLOSED fast path (no write contention)
     *   Phase 2: write lock → transition OPEN → CLOSED if cooldown elapsed
     *
     * @return true if request is allowed, false if circuit is OPEN
     */
    public boolean allowRequest() {

        // ── Phase 1: read lock — hot path ────────────────────
        rwLock.readLock().lock();
        try {
            if (state == State.CLOSED) return true;   // fast path

            // OPEN: check cooldown without write lock first
            long elapsed = now() - openedAtMs;
            if (elapsed < cooldownMs) return false;   // still cooling down

        } finally {
            rwLock.readLock().unlock();
        }

        // ── Phase 2: write lock — transition OPEN → CLOSED ───
        rwLock.writeLock().lock();
        try {
            // Double-check: another thread may have already transitioned
            if (state == State.OPEN) {
                long elapsed = now() - openedAtMs;
                if (elapsed >= cooldownMs) {
                    state      = State.CLOSED;
                    openedAtMs = 0L;
                    failureTimes.clear();
                    log("OPEN → CLOSED (cooldown elapsed)");
                    return true;
                }
                return false;   // still cooling down
            }
            return true;   // another thread already closed it
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Record a failure.
     *
     * Three-layer trip guard:
     *   Layer 1: evictStale BEFORE addLast
     *   Layer 2: size >= threshold
     *   Layer 3: isWithinWindow (defence in depth)
     */
    public void recordFailure() {
        rwLock.writeLock().lock();
        try {
            // Ignore failures when already OPEN
            if (state == State.OPEN) return;

            long nowMs = now();

            // Layer 1: evict BEFORE adding (critical ordering)
            evictStale(nowMs);

            // Record this failure
            failureTimes.addLast(nowMs);

            log("Failure recorded — window: "
                + failureTimes.size() + "/" + failureThreshold);

            // Layer 2 + 3: count check AND time span guard
            if (failureTimes.size() >= failureThreshold
                    && isWithinWindow(nowMs)) {
                state      = State.OPEN;
                openedAtMs = nowMs;
                failureTimes.clear();
                log("CLOSED → OPEN (threshold breached)");
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /** No-op in CLOSED. Cooldown + transition handled in allowRequest(). */
    public void recordSuccess() {
        // Intentional no-op:
        // Success does NOT reset the failure window.
        // A burst of failures still trips even with interleaved successes.
    }

    /** Current state — for health checks / metrics. */
    public State getState() {
        rwLock.readLock().lock();
        try {
            return state;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // ── private helpers ───────────────────────────────────────

    /**
     * Layer 1: remove failures older than X minutes.
     * peekFirst/pollFirst: O(1), no iterator allocation.
     * Boundary: failure at exactly cutoff → evicted (<=, not <).
     */
    private void evictStale(long nowMs) {
        long cutoff = nowMs - windowMs;
        while (!failureTimes.isEmpty()
               && failureTimes.peekFirst() <= cutoff) {
            failureTimes.pollFirst();
        }
    }

    /**
     * Layer 3: confirms oldest failure is within window.
     * Defence in depth against clock skew or eviction edge cases.
     */
    private boolean isWithinWindow(long nowMs) {
        if (failureTimes.isEmpty()) return false;
        return (nowMs - failureTimes.peekFirst()) <= windowMs;
    }

    private long now() {
        return clock.instant().toEpochMilli();
    }

    private void log(String msg) {
        System.out.printf("  [CB] %s%n", msg);
    }
}
