package com.example.lldtest.config;

/**
 * Immutable configuration for the TaskScheduler.
 * Builder pattern allows future extension without breaking existing callers.
 */
public final class SchedulerConfig {

    private final int numWorkerThreads;
    private final int workerQueueCapacity;

    private SchedulerConfig(Builder builder) {
        this.numWorkerThreads   = builder.numWorkerThreads;
        this.workerQueueCapacity = builder.workerQueueCapacity;
    }

    public int getNumWorkerThreads()    { return numWorkerThreads; }
    public int getWorkerQueueCapacity() { return workerQueueCapacity; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int numWorkerThreads    = Runtime.getRuntime().availableProcessors();
        private int workerQueueCapacity = 256;

        public Builder numWorkerThreads(int n) {
            if (n <= 0) throw new IllegalArgumentException("numWorkerThreads must be > 0");
            this.numWorkerThreads = n;
            return this;
        }

        public Builder workerQueueCapacity(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
            this.workerQueueCapacity = capacity;
            return this;
        }

        public SchedulerConfig build() { return new SchedulerConfig(this); }
    }
}
