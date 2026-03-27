package org.flipkart.circuitbreaker;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * A mutable Clock backed by a single Instant.
 *
 * Inject this into CircuitBreaker / WebClient to test
 * time-based transitions (OPEN → CLOSED after cooldown)
 * without Thread.sleep().
 *
 * Usage:
 *   TestClock clock = new TestClock(Instant.EPOCH);
 *   WebClient client = new WebClient(transport, configs, clock);
 *
 *   // advance 5 minutes
 *   clock.advance(5 * 60_000L);
 */
public class TestClock extends Clock {

    private Instant now;

    public TestClock(Instant start) {
        this.now = start;
    }

    /** Advance the clock forward by {@code millis} milliseconds. */
    public void advance(long millis) {
        now = now.plusMillis(millis);
    }

    /** Set clock to an absolute epoch-ms value. */
    public void advanceTo(long epochMs) {
        now = Instant.ofEpochMilli(epochMs);
    }

    /** Current epoch milliseconds. */
    public long millis() {
        return now.toEpochMilli();
    }

    @Override public Instant    instant() { return now; }
    @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
    @Override public Clock      withZone(java.time.ZoneId zone) { return this; }
}
