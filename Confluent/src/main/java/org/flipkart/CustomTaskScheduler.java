package org.flipkart;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomTaskScheduler {
    private final DelayQueue<ScheduledTask> delayQueue = new DelayQueue<>();
    private final ExecutorService workerPool;
    private final Thread dispatcherThread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    // Task wrapper with delay support
    private static class ScheduledTask implements Delayed {
        private final Runnable task;
        private final long startTime; // when to run

        public ScheduledTask(Runnable task, long delayMillis) {
            this.task = task;
            this.startTime = System.currentTimeMillis() + delayMillis;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long delay = startTime - System.currentTimeMillis();
            return unit.convert(delay, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(this.startTime, ((ScheduledTask) other).startTime);
        }
    }

    // Initialize scheduler with worker threads
    public CustomTaskScheduler(int workerThreads) {
        this.workerPool = Executors.newFixedThreadPool(workerThreads);

        // Consumer thread: picks ready tasks and runs them
        this.dispatcherThread = new Thread(() -> {
            while (running.get() || !delayQueue.isEmpty()) {
                try {
                    ScheduledTask task = delayQueue.take(); // blocks until task ready
                    workerPool.submit(task.task);
                } catch (InterruptedException e) {
                    if (!running.get()) break; // exit on shutdown
                }
            }
        });
        dispatcherThread.start();
    }

    // Producer API
    public void schedule(Runnable task, long delayMillis) {
        delayQueue.put(new ScheduledTask(task, delayMillis));
    }

    // Graceful shutdown
    public void shutdown() {
        running.set(false);
        dispatcherThread.interrupt(); // wake dispatcher if blocked
        workerPool.shutdown();
    }

    // Demo main
    public static void main(String[] args) throws InterruptedException {
        CustomTaskScheduler scheduler = new CustomTaskScheduler(2);

        // Multiple producers
        Thread producer1 = new Thread(() -> scheduler.schedule(() ->
                System.out.println(Thread.currentThread().getName() + " - Task A executed at " + System.currentTimeMillis()), 1000));

        Thread producer2 = new Thread(() -> scheduler.schedule(() ->
                System.out.println(Thread.currentThread().getName() + " - Task B executed at " + System.currentTimeMillis()), 500));

        producer1.start();
        producer2.start();

        producer1.join();
        producer2.join();

        Thread.sleep(2000);
        scheduler.shutdown();
    }
}
