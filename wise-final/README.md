# Circuit Breaker — Wise London Senior SDE Interview (Final Solution)

## What This Covers

```
✅ CLOSED → OPEN → HALF_OPEN → CLOSED  state machine
✅ Per-service config (each service own Y, X, Z)
✅ Thread safety (ReadWriteLock + AtomicBoolean + volatile)
✅ Sliding window with correct evict-before-add ordering
✅ Three-layer trip guard (evict + count + isWithinWindow)
✅ Injectable Clock (no Thread.sleep in tests)
✅ All fixes from code review applied
✅ Full JUnit 5 test suite including concurrency tests
✅ Simulation demo with 8 scenarios
```

---

## Run

```bash
mvn test        # all tests
mvn exec:java   # simulation demo
```

---

## File Structure

```
src/main/java/org/flipkart/circuitbreaker/
├── CircuitBreakerConfig.java   ← immutable per-service config + builder
├── CircuitBreakerState.java    ← state holder, lock lives WITH state
├── HttpClient.java             ← @FunctionalInterface transport
├── Request.java                ← outbound request model
├── Response.java               ← HTTP response (4xx ≠ failure)
├── ServiceUnavailableException ← carries service + circuit state
├── TestClock.java              ← injectable clock for tests
├── WebClient.java              ← MAIN ENTRY POINT: execute(Request)
└── Simulation.java             ← 8-scenario demo

src/test/java/org/flipkart/circuitbreaker/
└── WebClientTest.java          ← full JUnit 5 suite
```

---

## Key Design Decisions

### 1. Lock lives WITH state (CircuitBreakerState)
```java
final class CircuitBreakerState {
    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    volatile State state = State.CLOSED;
    final Deque<Long> failureTimestamps = new ArrayDeque<>();
    // ...
}
```
Impossible to access state without the lock being right there.

### 2. Two-phase lock in acquirePermission()
```
Phase 1: read lock  → fast path (CLOSED = return immediately, no contention)
Phase 2: write lock → only if transition needed
Double-check inside write lock — another thread may have transitioned
```

### 3. Three-layer trip guard
```
Layer 1: evictStaleFailures BEFORE addLast   ← order matters
Layer 2: size >= threshold
Layer 3: isWithinWindow (defence in depth)
```

### 4. Fixes over original code
```
Fix 1: clock.millis() → clock.instant().toEpochMilli()
Fix 2: evict BEFORE add (not after)
Fix 3: LinkedList → ArrayDeque (cache locality)
Fix 4: Iterator eviction → peekFirst/pollFirst
Fix 5: boolean probeInFlight → AtomicBoolean
```

### 5. 4xx does NOT trip the breaker
```java
public boolean isServerError() { return statusCode >= 500; }
// 400, 404, 422 = client error — server is healthy
```

---

## Thread-Safety Summary

| Concern | Solution |
|---|---|
| Hot path reads (CLOSED) | ReadLock — many threads simultaneously |
| State mutations | WriteLock — one thread at a time |
| Probe guard | AtomicBoolean CAS — exactly one probe |
| Visibility | volatile on state + openedAtMs |
| Breaker registry | ConcurrentHashMap.computeIfAbsent |
| HTTP call | No lock held — lock released before transport.send() |
