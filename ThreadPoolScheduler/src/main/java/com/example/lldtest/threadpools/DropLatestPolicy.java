package com.example.lldtest.threadpools;

import com.example.lldtest.task.ScheduledTask;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * UC4 Implementation: when the pool is saturated, compare the incoming
 * task against the latest-scheduled task currently waiting in the queue.
 * Keep whichever is scheduled EARLIER; drop the other.
 *
 * Rationale: "execute tasks with the latest execution time" = the task
 * furthest in the future is least urgent and therefore most expendable.
 */
public class DropLatestPolicy implements SaturationPolicy {

    @Override
    public Optional<ScheduledTask> resolve(
        ScheduledTask incoming,
        PriorityBlockingQueue<ScheduledTask> waitingQueue
    ) {
        // Find the task with the furthest execution time in the waiting queue
        Optional<ScheduledTask> latestWaiting = waitingQueue.stream()
            .max(Comparator.comparingLong(ScheduledTask::getExecutionTimeMs));

        if (latestWaiting.isEmpty()) {
            // Nothing waiting — all threads are active, no room
            // Drop incoming; it will miss its window
            log("No waiting tasks. Dropping incoming", incoming);
            return Optional.empty();
        }

        ScheduledTask latest = latestWaiting.get();

        if (latest.getExecutionTimeMs() > incoming.getExecutionTimeMs()) {
            // Latest waiting is FURTHER in future than incoming → drop it
            // and let incoming take its slot
            boolean removed = waitingQueue.remove(latest);
            if (removed) {
                log("Dropped waiting task (later)", latest);
                return Optional.of(incoming); // incoming takes the freed slot
            }
            // Race: latest was already picked up by a worker thread
            // Fall through to drop incoming
        }

        // Incoming is the latest (or equal) — drop it
        log("Dropped incoming task (latest)", incoming);
        return Optional.empty();
    }

    private void log(String reason, ScheduledTask task) {
        System.out.printf("[UC4][%s] %s due=%d%n",
            reason, task.getId().substring(0, 8), task.getExecutionTimeMs());
    }
}
