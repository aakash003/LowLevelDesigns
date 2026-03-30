package org.flipkart.circuitbreaker;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Holds all mutable state for one service's circuit breaker.
 *
 * Design decision: lock lives WITH the state it guards.
 * This makes it impossible to accidentally access state
 * without the correct lock — the lock is right there.
 *
 * States:
 *   CLOSED    → normal, allow all requests
 *   OPEN      → failing, block all requests
 *   HALF_OPEN → recovering, allow exactly one probe
 *
 * Thread-safety:
 *   ReadWriteLock → read  lock: allowRequest() in CLOSED (hot path)
 *                   write lock: all state mutations
 *   AtomicBoolean → probeInFlight: CAS ensures exactly one probe
 *                   even without holding a lock for this check
 *   volatile      → state + openedAtMs: visibility guarantee
 *                   for reads that happen before lock acquisition
 */
final class CircuitBreakerState {

    enum State { CLOSED, OPEN, HALF_OPEN }

    // ── config — immutable, no lock needed ───────────────────────────────
    final CircuitBreakerConfig config;
    final String               serviceName;

    // ── lock — guards all mutable fields below ───────────────────────────
    final ReentrantReadWriteLock lock   = new ReentrantReadWriteLock();
    final ReentrantReadWriteLock.ReadLock  rLock = lock.readLock();
    final ReentrantReadWriteLock.WriteLock wLock = lock.writeLock();

    // ── mutable state — always mutated under write lock ──────────────────
    volatile State state      = State.CLOSED;
    volatile long  openedAtMs = 0L;

    // Sliding window: epoch-ms timestamps of recent failures.
    // ArrayDeque chosen over LinkedList:
    //   → contiguous memory = cache friendly
    //   → no node allocations = less GC pressure
    //   → peekFirst/pollFirst O(1) — same as LinkedList but faster in practice
    final Deque<Long> failureTimestamps = new ArrayDeque<>();

    // Probe guard: AtomicBoolean allows CAS without holding write lock
    // for the initial check — prevents thundering herd in HALF_OPEN.
    // Always set to false under write lock after probe completes.
    final AtomicBoolean probeInFlight = new AtomicBoolean(false);

    CircuitBreakerState(String serviceName, CircuitBreakerConfig config) {
        this.serviceName = serviceName;
        this.config      = config;
    }
}
