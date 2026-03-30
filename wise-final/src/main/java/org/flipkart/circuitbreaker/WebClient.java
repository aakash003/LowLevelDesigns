package org.flipkart.circuitbreaker;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebClient — per-service circuit breaker with full thread safety.
 *
 * ═══════════════════════════════════════════════════════════════
 * STATE MACHINE (per service)
 * ═══════════════════════════════════════════════════════════════
 *
 *   CLOSED ──(Y failures in X min window)──► OPEN
 *   OPEN   ──(Z min cooldown elapsed)     ──► HALF_OPEN
 *   HALF_OPEN ──(probe succeeds)          ──► CLOSED
 *   HALF_OPEN ──(probe fails)             ──► OPEN  (fresh cooldown)
 *
 * ═══════════════════════════════════════════════════════════════
 * THREAD-SAFETY MODEL
 * ═══════════════════════════════════════════════════════════════
 *
 *   ReadWriteLock (per breaker, not global):
 *     read  lock → acquirePermission() in CLOSED (hot path)
 *                  many threads read simultaneously — zero contention
 *     write lock → all state transitions + failureTimestamps mutations
 *
 *   Two-phase lock pattern in acquirePermission():
 *     Phase 1: read lock  → fast path check (CLOSED = return immediately)
 *     Phase 2: write lock → transition only if state changed
 *     Double-check inside write lock — another thread may have transitioned
 *     while we waited to acquire write lock.
 *
 *   AtomicBoolean probeInFlight:
 *     → CAS ensures exactly ONE probe in HALF_OPEN
 *     → Prevents thundering herd when cooldown expires
 *
 *   volatile state + openedAtMs:
 *     → visibility guarantee for reads before lock acquisition
 *
 *   ConcurrentHashMap + computeIfAbsent:
 *     → per-service breakers created lazily, exactly once
 *     → SVC_B and SVC_C fully independent — no cross-service blocking
 *
 *   HTTP call made with NO lock held:
 *     → HTTP can take 100ms-30s — holding lock = system-wide stall
 *     → lock released before transport.send(), re-acquired in onSuccess/onFailure
 *
 * ═══════════════════════════════════════════════════════════════
 * THREE-LAYER TRIP GUARD in recordFailure()
 * ═══════════════════════════════════════════════════════════════
 *
 *   Layer 1: evictStaleFailures  → clean window (evict BEFORE adding)
 *   Layer 2: size >= threshold   → count check
 *   Layer 3: isWithinWindow      → time span guard (defence in depth)
 *
 * ═══════════════════════════════════════════════════════════════
 * FIXES OVER ORIGINAL VERSION
 * ═══════════════════════════════════════════════════════════════
 *
 *   Fix 1: clock.millis() → clock.instant().toEpochMilli()
 *           (Clock has no millis() method)
 *
 *   Fix 2: evictStale called BEFORE addLast, not after
 *           (evicting after means you count on stale data)
 *
 *   Fix 3: LinkedList → ArrayDeque
 *           (contiguous memory, no node allocations, cache friendly)
 *
 *   Fix 4: Iterator eviction → peekFirst/pollFirst
 *           (no iterator allocation, cleaner boundary handling)
 *
 *   Fix 5: boolean probeInFlight → AtomicBoolean
 *           (explicit visibility + intent, guards future bugs)
 */
public class WebClient {

    // ── registries ────────────────────────────────────────────────────────
    private final ConcurrentHashMap<String, CircuitBreakerConfig> configs
        = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, CircuitBreakerState> breakers
        = new ConcurrentHashMap<>();

    // ── dependencies ──────────────────────────────────────────────────────
    private final HttpClient httpClient;
    private final Clock      clock;

    // ── constructors ──────────────────────────────────────────────────────

    /** Production — system UTC clock. */
    public WebClient(HttpClient httpClient) {
        this(httpClient, Clock.systemUTC());
    }

    /** Tests — injectable clock, no Thread.sleep() needed. */
    public WebClient(HttpClient httpClient, Clock clock) {
        this.httpClient = httpClient;
        this.clock      = clock;
    }

    /**
     * Register per-service config BEFORE first request.
     * Unknown services automatically get CircuitBreakerConfig.defaults().
     */
    public void registerConfig(String service, CircuitBreakerConfig config) {
        configs.put(service, config);
    }

    // ═════════════════════════════════════════════════════════════════════
    // MAIN ENTRY POINT
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Execute request to downstream service, honouring its circuit breaker.
     *
     * Flow:
     *   ① acquirePermission() — fail fast if OPEN or probe busy
     *   ② httpClient.send()   — actual HTTP call (NO lock held)
     *   ③ onSuccess/onFailure — record outcome, update state
     *
     * @throws ServiceUnavailableException if circuit is OPEN or HALF_OPEN-busy
     */
    public Response execute(Request request) {
        String             service = request.getServiceName();
        CircuitBreakerState cb     = breakerFor(service);

        // ① Gate check — fail fast, no HTTP call made
        boolean isProbe = acquirePermission(cb);

        // ② HTTP call — NO lock held here (critical for performance)
        try {
            Response response = httpClient.send(request);

            // ③ Classify — only 5xx trips breaker, 4xx does not
            if (response.isServerError()) {
                onFailure(cb, isProbe);
            } else {
                onSuccess(cb, isProbe);
            }

            return response;

        } catch (Exception e) {
            // Network-level exception = server-side failure
            onFailure(cb, isProbe);
            throw e;
        }
    }

    /** Current state for a service — for health checks / metrics. */
    public CircuitBreakerState.State stateFor(String service) {
        CircuitBreakerState cb = breakers.get(service);
        if (cb == null) return CircuitBreakerState.State.CLOSED;

        cb.rLock.lock();
        try {
            return cb.state;
        } finally {
            cb.rLock.unlock();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // ACQUIRE PERMISSION
    //
    // Two-phase lock pattern:
    //   Phase 1: read lock  → fast path (CLOSED returns immediately)
    //   Phase 2: write lock → slow path (state transition if needed)
    //
    // Double-check in write lock: another thread may have transitioned
    // between us releasing read lock and acquiring write lock.
    //
    // Returns: true if this request is a HALF_OPEN probe, false otherwise.
    // ═════════════════════════════════════════════════════════════════════
    private boolean acquirePermission(CircuitBreakerState cb) {
        long now = clock.instant().toEpochMilli();   // Fix 1: no clock.millis()

        // ── Phase 1: read lock — hot path ────────────────────────────────
        cb.rLock.lock();
        try {
            switch (cb.state) {

                case CLOSED:
                    // Fast path — most common case, zero write contention
                    return false;

                case OPEN:
                    if (now - cb.openedAtMs < cb.config.cooldownMs()) {
                        // Still cooling down — reject immediately
                        throw new ServiceUnavailableException(
                            cb.serviceName,
                            CircuitBreakerState.State.OPEN,
                            "Circuit OPEN for '" + cb.serviceName
                            + "'. Retry in "
                            + ((cb.config.cooldownMs() - (now - cb.openedAtMs)) / 1_000)
                            + "s.");
                    }
                    // Cooldown elapsed — fall through to write lock for transition
                    break;

                case HALF_OPEN:
                    // CAS: if probe already in flight, reject without write lock
                    if (cb.probeInFlight.get()) {
                        throw new ServiceUnavailableException(
                            cb.serviceName,
                            CircuitBreakerState.State.HALF_OPEN,
                            "Circuit HALF_OPEN for '" + cb.serviceName
                            + "'. Probe already in flight.");
                    }
                    // Probe slot may be free — fall through to write lock to claim it
                    break;
            }
        } finally {
            cb.rLock.unlock();
        }

        // ── Phase 2: write lock — state transition ───────────────────────
        cb.wLock.lock();
        try {
            now = clock.instant().toEpochMilli();  // refresh — time passed waiting

            // Double-check: another thread may have already transitioned
            switch (cb.state) {

                case CLOSED:
                    // Another thread closed it while we waited — allow normally
                    return false;

                case OPEN:
                    if (now - cb.openedAtMs < cb.config.cooldownMs()) {
                        // Still cooling (concurrent thread re-opened it)
                        throw new ServiceUnavailableException(
                            cb.serviceName,
                            CircuitBreakerState.State.OPEN,
                            "Circuit OPEN for '" + cb.serviceName + "'.");
                    }
                    // Cooldown elapsed — transition OPEN → HALF_OPEN
                    cb.state = CircuitBreakerState.State.HALF_OPEN;
                    cb.probeInFlight.set(true);
                    log(cb.serviceName, "OPEN → HALF_OPEN (cooldown elapsed, probe started)");
                    return true;   // this thread is the probe

                case HALF_OPEN:
                    // CAS: claim probe slot atomically
                    if (cb.probeInFlight.compareAndSet(false, true)) {
                        return true;   // claimed — this thread is the probe
                    }
                    throw new ServiceUnavailableException(
                        cb.serviceName,
                        CircuitBreakerState.State.HALF_OPEN,
                        "Circuit HALF_OPEN for '" + cb.serviceName
                        + "'. Probe already in flight.");

                default:
                    return false;
            }
        } finally {
            cb.wLock.unlock();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // ON SUCCESS
    //
    // CLOSED + not probe: no-op — success doesn't reset failure window.
    //   Intentional: a burst of failures still trips even with successes.
    //
    // HALF_OPEN probe success: transition to CLOSED, clear window.
    // ═════════════════════════════════════════════════════════════════════
    private void onSuccess(CircuitBreakerState cb, boolean isProbe) {
        if (!isProbe) return;   // CLOSED success — intentional no-op

        cb.wLock.lock();
        try {
            cb.state = CircuitBreakerState.State.CLOSED;
            cb.failureTimestamps.clear();
            cb.openedAtMs = 0L;
            cb.probeInFlight.set(false);
            log(cb.serviceName, "HALF_OPEN → CLOSED (probe succeeded)");
        } finally {
            cb.wLock.unlock();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // ON FAILURE
    //
    // HALF_OPEN probe failure: immediately re-open with fresh cooldown.
    //   No need to fill sliding window — one failed probe is enough.
    //
    // CLOSED failure: three-layer trip guard:
    //   Layer 1: evictStaleFailures BEFORE addLast  ← Fix 2
    //   Layer 2: size >= threshold  (count check)
    //   Layer 3: isWithinWindow     (time span guard — defence in depth)
    // ═════════════════════════════════════════════════════════════════════
    private void onFailure(CircuitBreakerState cb, boolean isProbe) {
        cb.wLock.lock();
        try {
            long now = clock.instant().toEpochMilli();   // Fix 1

            if (isProbe) {
                // Probe failed → re-open with fresh cooldown timer
                cb.state      = CircuitBreakerState.State.OPEN;
                cb.openedAtMs = now;
                cb.probeInFlight.set(false);
                log(cb.serviceName, "HALF_OPEN → OPEN (probe failed, fresh cooldown)");
                return;
            }

            // Only record failures in CLOSED state
            if (cb.state != CircuitBreakerState.State.CLOSED) return;

            // Layer 1: evict BEFORE adding ← Fix 2 (original had addLast then evict)
            evictStaleFailures(cb, now);

            // Record this failure
            cb.failureTimestamps.addLast(now);

            log(cb.serviceName, "Failure recorded — window count: "
                + cb.failureTimestamps.size()
                + "/" + cb.config.failureThreshold());

            // Layer 2 + 3: count check AND time span guard
            if (cb.failureTimestamps.size() >= cb.config.failureThreshold()
                    && isWithinWindow(cb, now)) {
                cb.state      = CircuitBreakerState.State.OPEN;
                cb.openedAtMs = now;
                cb.failureTimestamps.clear();
                log(cb.serviceName, "CLOSED → OPEN (threshold breached)");
            }

        } finally {
            cb.wLock.unlock();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // SLIDING WINDOW HELPERS
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Layer 1: remove failures older than X minutes from front of deque.
     *
     * Uses peekFirst/pollFirst (Fix 4) — no Iterator allocation,
     * cleaner boundary handling than Iterator approach.
     *
     * Boundary: failure at cutoff exactly → evicted (<=, not <).
     *   e.g. failure at T+0, window=10min, now=T+10min
     *        cutoff = T+0, peekFirst = T+0 <= T+0 → evicted ✅
     *
     * Always called under write lock.
     */
    private void evictStaleFailures(CircuitBreakerState cb, long nowMs) {
        long cutoff = nowMs - cb.config.windowMs();
        while (!cb.failureTimestamps.isEmpty()
               && cb.failureTimestamps.peekFirst() <= cutoff) {
            cb.failureTimestamps.pollFirst();
        }
    }

    /**
     * Layer 3: confirms oldest failure in deque is within the window.
     *
     * Defence in depth against:
     *   - Clock skew between eviction and size check
     *   - Future bugs in eviction logic
     *   - Edge cases at exact window boundaries
     *
     * Always called under write lock.
     */
    private boolean isWithinWindow(CircuitBreakerState cb, long nowMs) {
        if (cb.failureTimestamps.isEmpty()) return false;
        return (nowMs - cb.failureTimestamps.peekFirst()) <= cb.config.windowMs();
    }

    // ═════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Lazily create a CircuitBreakerState per service.
     * computeIfAbsent is atomic — breaker created exactly once
     * even if 1000 threads call simultaneously.
     * Unregistered services get CircuitBreakerConfig.defaults().
     */
    private CircuitBreakerState breakerFor(String service) {
        return breakers.computeIfAbsent(service, svc -> {
            CircuitBreakerConfig cfg = configs.getOrDefault(
                svc, CircuitBreakerConfig.defaults());
            log(svc, "Breaker created — " + cfg);
            return new CircuitBreakerState(svc, cfg);
        });
    }

    private void log(String service, String msg) {
        System.out.printf("  [CB:%-8s] %s%n", service, msg);
    }
}
