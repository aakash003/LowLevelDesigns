package com.example.lldtest.threadpools;


import com.example.lldtest.config.SchedulerConfig;
import com.example.lldtest.task.ScheduledTask;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TaskScheduler: orchestrates timing and execution.
 *
 * Architecture:
 *
 *   schedule(task)
 *       │
 *       ▼
 *   DelayQueue<ScheduledTask>          ← PRODUCER SIDE
 *       │
 *       │  Single dispatcher thread
 *       │  blocks here (zero CPU) until
 *       │  next task is due
 *       ▼
 *   WorkerPool.submit(task)            ← CONSUMER SIDE
 *       │
 *       ├── thread free? → execute immediately
 *       │
 *       └── saturated?  → SaturationPolicy.resolve()  ← UC4
 *                              │
 *                              └── drop latest, keep earlier
 *
 *   After execution:
 *       └── recurring? → schedule() back into DelayQueue
 *
 * Why a single dispatcher thread?
 *   The dispatcher does NOTHING except: take() + submit().
 *   This is ~microseconds per task. A single thread handles
 *   100k+ tasks/second comfortably. Multiple dispatcher threads
 *   would cause contention on DelayQueue with no benefit.
 *
 * Why DelayQueue (not ScheduledThreadPoolExecutor)?
 *   STPE does not allow queue replacement, making UC4 impossible
 *   without reimplementing it. DelayQueue gives us timing precision
 *   (uses Condition.awaitNanos internally) while keeping UC4 accessible.
 */
public class TaskScheduler {

    private final DelayQueue<ScheduledTask> delayQueue = new DelayQueue<>();
    private final WorkerPool workerPool;
    private final Thread dispatcher;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public TaskScheduler(SchedulerConfig config) {
        this(config, new DropLatestPolicy());
    }

    /**
     * Constructor with custom saturation policy (for testing / extensibility).
     */
    public TaskScheduler(SchedulerConfig config, SaturationPolicy saturationPolicy) {
        this.workerPool = new WorkerPool(
            config.getNumWorkerThreads(),
            config.getWorkerQueueCapacity(),
            saturationPolicy
        );
        this.dispatcher = new Thread(this::dispatchLoop, "task-dispatcher");
        this.dispatcher.setDaemon(true);
    }

    // ── Public API ───────────────────────────────────────────────

    public TaskScheduler start() {
        if (running.compareAndSet(false, true)) {
            dispatcher.start();
        }
        return this;
    }

    /**
     * Schedule a task. Thread-safe; can be called from any thread at any time.
     */
    public void schedule(ScheduledTask task) {
        if (!running.get()) throw new IllegalStateException("Scheduler not started");
        delayQueue.offer(task);
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            dispatcher.interrupt();
            workerPool.shutdown();
        }
    }

    // ── Dispatcher loop ──────────────────────────────────────────

    /**
     * Single dispatcher thread — blocks precisely until the next task
     * is due, then hands it off to the worker pool immediately.
     *
     * This thread does NO execution work — it only dequeues and submits.
     */
    private void dispatchLoop() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                // Blocks here with ZERO CPU until earliest task is due.
                // Uses Condition.awaitNanos internally — JVM-precise timing.
                ScheduledTask task = delayQueue.take();

                // Hand off to worker pool — returns immediately
                // UC4 is enforced inside workerPool.submit()
                workerPool.submit(task, () -> rescheduleIfRecurring(task));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void rescheduleIfRecurring(ScheduledTask task) {
        task.nextTask().ifPresent(this::schedule);
    }

    // ── Observability ────────────────────────────────────────────

    public int getPendingTaskCount()  { return delayQueue.size(); }
    public int getActiveWorkerCount() { return workerPool.getActiveCount(); }
    public boolean isSaturated()      { return workerPool.isSaturated(); }
}
