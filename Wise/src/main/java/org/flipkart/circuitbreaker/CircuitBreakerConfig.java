package org.flipkart.circuitbreaker;

/**
 * Immutable configuration for one service's circuit breaker.
 *
 * Each service can have its own rulebook:
 *   failureThreshold → Y: how many failures before OPEN
 *   windowMillis     → X: sliding window duration
 *   cooldownMillis   → Z: how long to stay OPEN
 *
 * Use the fluent Builder for readable construction.
 */
public record CircuitBreakerConfig(
        int  failureThreshold,
        long windowMillis,
        long cooldownMillis
) {
    // ── validation in compact constructor ────────────────────────────────
    public CircuitBreakerConfig {
        if (failureThreshold <= 0)
            throw new IllegalArgumentException("failureThreshold must be > 0");
        if (windowMillis <= 0)
            throw new IllegalArgumentException("windowMillis must be > 0");
        if (cooldownMillis <= 0)
            throw new IllegalArgumentException("cooldownMillis must be > 0");
    }

    // ── sensible defaults ─────────────────────────────────────────────────
    public static CircuitBreakerConfig defaultConfig() {
        return new CircuitBreakerConfig(3, 10 * 60_000L, 5 * 60_000L);
    }

    // ── fluent builder ────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int  failureThreshold = 3;
        private long windowMillis     = 10 * 60_000L;
        private long cooldownMillis   =  5 * 60_000L;

        public Builder failureThreshold(int v)  { this.failureThreshold = v; return this; }
        public Builder windowMinutes(int min)   { this.windowMillis     = min * 60_000L; return this; }
        public Builder cooldownMinutes(int min) { this.cooldownMillis   = min * 60_000L; return this; }

        public CircuitBreakerConfig build() {
            return new CircuitBreakerConfig(failureThreshold, windowMillis, cooldownMillis);
        }
    }
}
