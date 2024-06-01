package org.flipkart.entity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActorSystem {
    private final ExecutorService executorService;
    private boolean isShutdown = false;

    public ActorSystem(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void execute(Runnable task) {
        if (!isShutdown) {
            executorService.execute(task);
        }
    }

    public void shutdown() {
        isShutdown = true;
        executorService.shutdown();
    }
}