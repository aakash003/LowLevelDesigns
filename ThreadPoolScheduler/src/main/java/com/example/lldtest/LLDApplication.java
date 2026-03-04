package com.example.lldtest;

import com.example.lldtest.config.SchedulerConfig;
import com.example.lldtest.task.OneTimeTask;
import com.example.lldtest.task.RecurringTask;
import com.example.lldtest.threadpools.TaskScheduler;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LLDApplication {
    static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    public static void main(String[] args) throws InterruptedException {

        // ── UC3: Configure number of worker threads ──────────────
        SchedulerConfig config = SchedulerConfig.builder()
                .numWorkerThreads(3)
                .workerQueueCapacity(10)
                .build();

        TaskScheduler scheduler = new TaskScheduler(config).start();

        // ── UC1: One-time task ────────────────────────────────────
        scheduler.schedule(new OneTimeTask(
                () -> log("ONE-TIME task executed"),
                System.currentTimeMillis() + 1000  // 1 second from now
        ));

        // ── UC2: Recurring task ───────────────────────────────────
        scheduler.schedule(new RecurringTask(
                () -> log("RECURRING task executed"),
                System.currentTimeMillis() + 500,  // first run in 500ms
                1000                               // then every 1 second
        ));

        // ── UC4: Saturation scenario ──────────────────────────────
        // Flood with slow tasks to saturate the 3-thread pool,
        // then schedule tasks at different future times.
        // Tasks with the latest execution time should be dropped.
        log("Scheduling saturation scenario...");

        // Fill all 3 worker threads with slow tasks (5 seconds each)
        for (int i = 0; i < 3; i++) {
            int taskNum = i;
            scheduler.schedule(new OneTimeTask(
                    () -> {
                        log("SLOW-TASK-" + taskNum + " started (will block thread for 3s)");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        log("SLOW-TASK-" + taskNum + " finished");
                    },
                    System.currentTimeMillis() + 100
            ));
        }

        // Give slow tasks time to start and saturate the pool
        Thread.sleep(300);

        long now = System.currentTimeMillis();

        // Schedule tasks at DIFFERENT future times while pool is saturated.
        // UC4: task at +10s should be dropped in favor of task at +2s.
        scheduler.schedule(new OneTimeTask(
                () -> log("URGENT task (due in 2s) — should survive UC4"),
                now + 2000
        ));

        scheduler.schedule(new OneTimeTask(
                () -> log("LATE task (due in 10s) — should be DROPPED by UC4"),
                now + 10_000
        ));

        scheduler.schedule(new OneTimeTask(
                () -> log("MEDIUM task (due in 5s)"),
                now + 5000
        ));

        // Let the scheduler run for 15 seconds total
        Thread.sleep(15_000);
        scheduler.stop();
        log("Scheduler stopped. Pending tasks: " + scheduler.getPendingTaskCount());
    }

    static void log(String msg) {
        System.out.printf("[%s][%s] %s%n",
                FMT.format(Instant.now()),
                Thread.currentThread().getName(),
                msg);
    }
}


/**
 * Problem Statement: Design a multi-threaded task scheduler with n threads which has the capability to schedule recurring tasks or one-time tasks.
 * <p>
 * Use Cases:
 * <p>
 * Job scheduled at a particular time
 * A recurring job with a particular interval of recurrence.
 * The user should be able to configure the number of worker threads
 * When there are an insufficient number of idle threads to perform the execution of all the tasks, we are going to execute the tasks with the latest execution time.
 * <p>
 * <p>
 * <p>
 * ## The Mental Model
 * ```
 * STPE handles:              Semaphore handles:
 * ┌─────────────────┐        ┌──────────────────────────────┐
 * │ - Timing        │        │ - How many run concurrently  │
 * │ - Recurring via │        │ - UC4 drop policy            │
 * │   reschedule()  │        │ - Backpressure               │
 * │ - Thread mgmt   │        └──────────────────────────────┘
 * └─────────────────┘
 * │                              │
 * └──────────── task ────────────┘
 * attemptExecution()
 * bridges both
 * ```
 */

