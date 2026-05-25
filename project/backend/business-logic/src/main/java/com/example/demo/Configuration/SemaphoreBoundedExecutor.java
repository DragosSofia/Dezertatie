package com.example.demo.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public final class SemaphoreBoundedExecutor implements Executor, AutoCloseable {

    private final ExecutorService delegate;
    private final Semaphore semaphore;

    public SemaphoreBoundedExecutor(ExecutorService delegate, int permits) {
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be > 0, was " + permits);
        }
        this.delegate = delegate;
        this.semaphore = new Semaphore(permits);
    }

    @Override
    public void execute(Runnable task) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RejectedExecutionException("Interrupted while waiting for permit", e);
        }
        boolean submitted = false;
        try {
            delegate.execute(() -> {
                try {
                    task.run();
                } finally {
                    semaphore.release();
                }
            });
            submitted = true;
        } finally {
            if (!submitted) {
                semaphore.release();
            }
        }
    }

    public int availablePermits() {
        return semaphore.availablePermits();
    }

    public int queueLength() {
        return semaphore.getQueueLength();
    }

    @Override
    public void close() {
        delegate.shutdown();
        try {
            if (!delegate.awaitTermination(30, TimeUnit.SECONDS)) {
                delegate.shutdownNow();
            }
        } catch (InterruptedException e) {
            delegate.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
