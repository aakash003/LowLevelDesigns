package org.flipkart.circuitbreaker;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Controllable clock for deterministic testing.
 *
 * Inject into WebClient so tests can advance time without Thread.sleep().
 *
 * Usage:
 *   TestClock clock  = new TestClock(Instant.EPOCH);
 *   WebClient client = new WebClient(httpClient, clock);
 *
 *   clock.advance(5 * 60_000L);   // simulate 5 minutes passing instantly
 */
public final class TestClock extends Clock {

    private Instant now;

    public TestClock(Instant start) {
        this.now = start;
    }

    /** Advance clock forward by {@code millis} milliseconds. */
    public void advance(long millis) {
        now = now.plusMillis(millis);
    }

    /** Set clock to an absolute epoch-ms offset. */
    public void advanceTo(long epochMs) {
        now = Instant.ofEpochMilli(epochMs);
    }

    /** Current epoch milliseconds. */
    public long millis() { return now.toEpochMilli(); }

    @Override public Instant    instant() { return now; }
    @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
    @Override public Clock      withZone(java.time.ZoneId zone) { return this; }
}
