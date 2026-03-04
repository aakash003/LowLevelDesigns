package com.example.lldtest.task;

import com.example.lldtest.ExecutionContext;

import java.util.Optional;

public class RecurringTask  extends ScheduledTask {
    private final long intervalMs;

    public RecurringTask(ExecutionContext context, long executionTimeMs, long intervalMs) {
        super(context, executionTimeMs);
        this.intervalMs = intervalMs;
    }

    @Override
    public boolean isRecurring() {
        return true;
    }

    @Override
    public Optional<ScheduledTask> nextTask() {
        // Fixed-delay: next run is interval ms from NOW (after execution completes)
        return Optional.of(
                new RecurringTask(context, System.currentTimeMillis() + intervalMs, intervalMs)
        );
    }
}
