package org.flipkart.circuitbreaker;

/**
 * Immutable per-service circuit breaker configuration.
 *
 * Each service gets its own rulebook:
 *   failureThreshold → Y: failures before OPEN
 *   windowMs         → X: sliding window duration
 *   cooldownMs       → Z: how long to stay OPEN
 *
 * Validated at construction — fail fast on bad config.
 * Use the fluent Builder for readable construction at call site.
 */
public final class CircuitBreakerConfig {

    private final int  failureThreshold;
    private final long windowMs;
    private final long cooldownMs;

    public CircuitBreakerConfig(int failureThreshold, long windowMs, long cooldownMs) {
        if (failureThreshold <= 0)
            throw new IllegalArgumentException(
                "failureThreshold must be > 0, got: " + failureThreshold);
        if (windowMs <= 0)
            throw new IllegalArgumentException(
                "windowMs must be > 0, got: " + windowMs);
        if (cooldownMs <= 0)
            throw new IllegalArgumentException(
                "cooldownMs must be > 0, got: " + cooldownMs);

        this.failureThreshold = failureThreshold;
        this.windowMs         = windowMs;
        this.cooldownMs       = cooldownMs;
    }

    public int  failureThreshold() { return failureThreshold; }
    public long windowMs()         { return windowMs; }
    public long cooldownMs()       { return cooldownMs; }

    /** Sensible default — used for unknown/unregistered services. */
    public static CircuitBreakerConfig defaults() {
        return new CircuitBreakerConfig(3, 10 * 60_000L, 5 * 60_000L);
    }

    /** Fluent builder for readable construction. */
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int  failureThreshold = 3;
        private long windowMs         = 10 * 60_000L;
        private long cooldownMs       =  5 * 60_000L;

        public Builder failureThreshold(int v)  { this.failureThreshold = v;       return this; }
        public Builder windowMinutes(int min)   { this.windowMs  = min * 60_000L;  return this; }
        public Builder cooldownMinutes(int min) { this.cooldownMs = min * 60_000L; return this; }

        public CircuitBreakerConfig build() {
            return new CircuitBreakerConfig(failureThreshold, windowMs, cooldownMs);
        }
    }

    @Override
    public String toString() {
        return String.format("CircuitBreakerConfig{threshold=%d, window=%dmin, cooldown=%dmin}",
            failureThreshold, windowMs / 60_000, cooldownMs / 60_000);
    }
}
