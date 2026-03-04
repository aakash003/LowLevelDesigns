package com.example.lldtest.threadpools;



import com.example.lldtest.task.ScheduledTask;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages a fixed pool of worker threads.
 *
 * Key design decisions:
 *
 * 1. Uses a PriorityBlockingQueue internally so waiting tasks
 *    are always ordered earliest-first. This means the "latest"
 *    task is always identifiable for UC4 without scanning.
 *
 * 2. UC4 is enforced HERE, not in the dispatcher — the pool owns
 *    the decision about what runs, the dispatcher just says "this is due".
 *
 * 3. Semaphore tracks ACTIVE executions (not queue size) so we know
 *    precisely when all threads are busy vs just when queue is full.
 */
public class WorkerPool {

    private final int numThreads;
    private ThreadPoolExecutor executor;
    private final PriorityBlockingQueue<ScheduledTask> waitingQueue;
    private final Semaphore activePermits;
    private final SaturationPolicy saturationPolicy;

    // Named thread factory for observability
    private static final AtomicInteger POOL_COUNTER = new AtomicInteger(0);

    public WorkerPool(int numThreads, int queueCapacity, SaturationPolicy saturationPolicy) {
        this.numThreads        = numThreads;
        this.saturationPolicy  = saturationPolicy;
        this.activePermits     = new Semaphore(numThreads);

        // Earliest-first ordering — latest task is always at the tail
        this.waitingQueue = new PriorityBlockingQueue<>(
            queueCapacity,
            Comparator.comparingLong(ScheduledTask::getExecutionTimeMs)
        );

        int poolId = POOL_COUNTER.incrementAndGet();
        this.executor = new ThreadPoolExecutor(
            numThreads, numThreads,
            0L, TimeUnit.MILLISECONDS,
            // Worker threads pull directly from our priority queue
            new PriorityBlockingQueue<>(
                queueCapacity,
                Comparator.comparingLong(r -> {
                    // Unwrap to get execution time for ordering
                    if (r instanceof TaskRunnable tr) return tr.getExecutionTimeMs();
                    return Long.MAX_VALUE;
                })
            ),
            r -> {
                Thread t = new Thread(r, String.format("worker-%d-%d", poolId,
                    ((ThreadPoolExecutor) executor).getPoolSize()));
                t.setDaemon(true);
                return t;
            }
        );
    }

    /**
     * Submit a task for execution, applying UC4 if pool is saturated.
     *
     * @param task      the task to run
     * @param onReschedule called after execution — scheduler uses this to reschedule recurring tasks
     */
    public void submit(ScheduledTask task, Runnable onReschedule) {
        boolean acquired = activePermits.tryAcquire();

        if (acquired) {
            // Thread available — execute immediately
            executeOn(task, onReschedule, true /* already acquired */);
        } else {
            // All threads busy — UC4 decision
            Optional<ScheduledTask> winner = saturationPolicy.resolve(task, waitingQueue);
            winner.ifPresent(t -> waitingQueue.offer(t));
            // Worker threads will drain waitingQueue as permits free up
        }
    }

    private void executeOn(ScheduledTask task, Runnable onReschedule, boolean permitHeld) {
        executor.execute(new TaskRunnable(task, () -> {
            try {
                task.execute();
            } catch (Exception e) {
                System.err.printf("[WorkerPool] Task %s failed: %s%n",
                    task.getId().substring(0, 8), e.getMessage());
            } finally {
                onReschedule.run();
                activePermits.release();
                // Pick up next waiting task if any
                drainWaiting(onReschedule);
            }
        }));
    }

    /**
     * After a task finishes, check if anything is waiting and run it.
     * This is how waiting tasks eventually get executed without a separate dispatcher loop.
     */
    private void drainWaiting(Runnable defaultReschedule) {
        ScheduledTask next = waitingQueue.poll();
        if (next == null) return;

        if (activePermits.tryAcquire()) {
            final ScheduledTask captured = next;
            executeOn(captured, defaultReschedule, true);
        } else {
            // No permit — put it back
            waitingQueue.offer(next);
        }
    }

    public boolean isSaturated() {
        return activePermits.availablePermits() == 0;
    }

    public int getActiveCount() {
        return numThreads - activePermits.availablePermits();
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ── Internal wrapper to carry executionTime through ThreadPoolExecutor ──

    static class TaskRunnable implements Runnable {
        private final ScheduledTask task;
        private final Runnable work;

        TaskRunnable(ScheduledTask task, Runnable work) {
            this.task = task;
            this.work = work;
        }

        long getExecutionTimeMs() { return task.getExecutionTimeMs(); }

        @Override
        public void run() { work.run(); }
    }
}
