package com.example.lldtest.task;

import com.example.lldtest.ExecutionContext;

import java.util.Optional;

public class OneTimeTask extends ScheduledTask {

    public OneTimeTask(ExecutionContext context, long executionTimeMs) {
        super(context, executionTimeMs);
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public Optional<ScheduledTask> nextTask() {
        return Optional.empty(); // No follow-up
    }
}
