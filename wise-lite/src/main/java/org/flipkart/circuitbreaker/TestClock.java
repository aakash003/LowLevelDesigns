package org.flipkart.circuitbreaker;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Controllable clock for deterministic tests.
 * Inject into WebClient — call advance() instead of Thread.sleep().
 */
public final class TestClock extends Clock {

    private Instant now;

    public TestClock(Instant start)  { this.now = start; }
    public void advance(long millis) { now = now.plusMillis(millis); }
    public long millis()             { return now.toEpochMilli(); }

    @Override public Instant    instant() { return now; }
    @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
    @Override public Clock      withZone(java.time.ZoneId z) { return this; }
}
