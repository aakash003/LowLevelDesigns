package org.flipkart.circuitbreaker;

/**
 * Immutable per-service config.
 *
 *   failureThreshold → Y: failures before OPEN
 *   windowMs         → X: sliding window duration
 *   cooldownMs       → Z: how long to stay OPEN
 */
public final class CircuitBreakerConfig {

    final int  failureThreshold;
    final long windowMs;
    final long cooldownMs;

    public CircuitBreakerConfig(int failureThreshold,
                                long windowMs,
                                long cooldownMs) {
        if (failureThreshold <= 0)
            throw new IllegalArgumentException("failureThreshold must be > 0");
        if (windowMs <= 0)
            throw new IllegalArgumentException("windowMs must be > 0");
        if (cooldownMs <= 0)
            throw new IllegalArgumentException("cooldownMs must be > 0");

        this.failureThreshold = failureThreshold;
        this.windowMs         = windowMs;
        this.cooldownMs       = cooldownMs;
    }

    /** Sensible default for unknown/unregistered services. */
    public static CircuitBreakerConfig defaults() {
        return new CircuitBreakerConfig(3, 10 * 60_000L, 5 * 60_000L);
    }

    @Override
    public String toString() {
        return String.format(
            "Config{threshold=%d, window=%dmin, cooldown=%dmin}",
            failureThreshold, windowMs / 60_000, cooldownMs / 60_000);
    }
}
