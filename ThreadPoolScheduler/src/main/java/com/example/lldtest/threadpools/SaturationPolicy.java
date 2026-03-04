package com.example.lldtest.threadpools;

import com.example.lldtest.task.ScheduledTask;

import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Strategy interface for UC4: what to do when the worker pool is saturated.
 *
 * Decouples the DROP DECISION from the scheduler itself,
 * making it easy to swap policies (drop latest, drop oldest,
 * drop by priority, etc.) without touching core scheduler logic.
 */
public interface SaturationPolicy {

    /**
     * Called when all worker threads are busy and the incoming task
     * cannot be immediately executed.
     *
     * @param incoming     the task that just became due but has no thread
     * @param waitingQueue live view of tasks waiting in the worker queue
     * @return the task that SHOULD be executed (either incoming or one
     *         from the queue), or empty to drop incoming silently
     */
    Optional<ScheduledTask> resolve(
        ScheduledTask incoming,
        PriorityBlockingQueue<ScheduledTask> waitingQueue
    );
}
