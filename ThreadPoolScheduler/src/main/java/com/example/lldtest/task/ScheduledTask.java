package com.example.lldtest.task;

import com.example.lldtest.ExecutionContext;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class ScheduledTask implements Delayed {

    private final String id;
    protected final ExecutionContext context;
    private final long executionTimeMs; // absolute epoch ms

    protected ScheduledTask(ExecutionContext context, long executionTimeMs) {
        this.id = UUID.randomUUID().toString();
        this.context = context;
        this.executionTimeMs = executionTimeMs;
    }

    // ── Core contract ────────────────────────────────────────────

    public abstract boolean isRecurring();

    /**
     * For recurring tasks: returns the next instance of this task.
     * For one-time tasks: returns empty.
     *
     * Note: uses System.currentTimeMillis() + interval (not executionTimeMs + interval)
     * to avoid drift accumulation over many recurrences.
     */
    public abstract Optional<ScheduledTask> nextTask();

    public void execute() {
        context.execute();
    }

    // ── Timing ───────────────────────────────────────────────────

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Delayed implementation — how long until this task is due.
     * DelayQueue.take() uses this to block the dispatcher thread
     * precisely until the next task is ready.
     */
    @Override
    public long getDelay(TimeUnit unit) {
        long remainingMs = executionTimeMs - System.currentTimeMillis();
        return unit.convert(remainingMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Comparator for DelayQueue's internal heap.
     * Earliest task = highest priority = dequeued first.
     */
    @Override
    public int compareTo(Delayed other) {
        return Long.compare(
                this.getDelay(TimeUnit.MILLISECONDS),
                other.getDelay(TimeUnit.MILLISECONDS)
        );
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s, due=%d]",
                getClass().getSimpleName(), id.substring(0, 8), executionTimeMs);
    }
}
