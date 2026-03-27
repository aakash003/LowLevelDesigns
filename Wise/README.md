# Circuit Breaker — Wise London Senior SDE Interview

## Problem Statement
> In a network of multiple services, for any service-service communication via APIs,
> the Circuit Breaker should open the circuit and API should fail-fast if the API error
> count for last **X minutes >= Y**. After a cool-off period of **Z minutes**, the
> circuit should close and the service should start receiving requests again.
> Only OPEN and CLOSED states (no HALF_OPEN).

---

## Project Structure

```
src/
├── main/java/org/flipkart/circuitbreaker/
│   ├── CircuitBreaker.java         ← core state machine
│   ├── CircuitBreakerConfig.java   ← per-service config record + builder
│   ├── CircuitOpenException.java   ← thrown when circuit is OPEN
│   ├── Request.java                ← outbound HTTP request model
│   ├── Response.java               ← HTTP response model
│   ├── TestClock.java              ← injectable clock for tests
│   ├── WebClient.java              ← entry point: execute(Request)
│   └── Simulation.java             ← runnable demo
└── test/java/org/flipkart/circuitbreaker/
    └── WebClientCircuitBreakerTest.java   ← full JUnit 5 test suite
```

---

## How to Run

```bash
# Run tests
mvn test

# Run simulation demo
mvn exec:java
```

---

## State Machine

```
CLOSED ──(Y failures in last X min)──► OPEN
OPEN   ──(Z min cooldown elapsed)   ──► CLOSED
```

---

## Key Design Decisions

### 1. Three-Layer Trip Guard
```java
// Layer 1: evict failures outside X-min window
evictStaleFailures(nowMs);

// Layer 2: count check
failureTimes.size() >= failureThreshold

// Layer 3: time span guard (defence in depth)
isWithinWindow(nowMs)
```

### 2. Thread-Safety
```
ReadWriteLock       → many threads read (CLOSED fast path)
                      one thread writes (state transitions)
volatile            → state + openedAt visible across threads
ConcurrentHashMap   → thread-safe per-service breaker registry
Two-phase locking   → snapshot(read) → transition(write)
                      avoids ReentrantReadWriteLock double-unlock bug
```

### 3. Per-Service Config
```java
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
WebClient client = new WebClient(transport, configs);
```

### 4. Injectable Clock (Testability)
```java
// Tests — no Thread.sleep() needed
TestClock clock = new TestClock(Instant.EPOCH);
WebClient client = new WebClient(transport, configs, clock);
clock.advance(5 * 60_000L);   // simulate 5 minutes passing
```

---

## Test Coverage

| Test | What it covers |
|---|---|
| startsInClosedState | Initial state |
| twoFailures_doNotTrip | Below threshold |
| exactlyThreeFailures_tripsBreaker | Boundary check |
| blocksRequests_whenOpen | OPEN blocks all |
| doesNotClose_beforeCooldown | Time-based |
| closes_afterCooldown | Recovery — time-based |
| serviceIsolation | SVC_B ≠ SVC_C |
| staleFailures_evicted | Sliding window |
| failureAtExactBoundary_evicted | Window boundary |
| networkException_countsAsFailure | Exception handling |
| clientError_doesNotTrip | 4xx ≠ server failure |
| unknownService_usesDefaultConfig | Fallback config |
| perServiceConfig_differentThresholds | Per-service isolation |
