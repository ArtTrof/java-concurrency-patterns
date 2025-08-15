package app.patterns.task_execution.task;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EnhancedTaskExecutor {
    private final ThreadPoolExecutor executor;

    public enum Priority {HIGH, NORMAL, LOW}

    public EnhancedTaskExecutor(int corePoolSize) {
        // Create priority-based executor
        BlockingQueue<Runnable> workQueue = new PriorityBlockingQueue<>();
        this.executor = new ThreadPoolExecutor(
                corePoolSize,
                corePoolSize,
                0L, TimeUnit.MILLISECONDS,
                workQueue,
                new PriorityThreadFactory()
        );
    }

    // Priority Task Wrapper
    private static class PriorityTask<T> implements Runnable, Comparable<PriorityTask<?>> {
        private final Callable<T> task;
        private final Priority priority;
        private final FutureTask<T> futureTask;

        PriorityTask(Callable<T> task, Priority priority) {
            this.task = task;
            this.priority = priority;
            this.futureTask = new FutureTask<>(task);
        }

        @Override
        public void run() {
            futureTask.run();
        }

        @Override
        public int compareTo(PriorityTask<?> other) {
            return other.priority.ordinal() - this.priority.ordinal();
        }

        public Future<T> getFuture() {
            return futureTask;
        }
    }

    // Thread factory with priority support
    private static class PriorityThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("PriorityWorker-" + counter.incrementAndGet());
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

    // Submit task with priority
    public <T> Future<T> submit(Callable<T> task, Priority priority) {
        PriorityTask<T> priorityTask = new PriorityTask<>(task, priority);
        executor.execute(priorityTask);
        return priorityTask.getFuture();
    }

    // Progress tracking
    public static class ProgressTask<T> implements Callable<T> {
        private final Callable<T> actualTask;
        private final Consumer<Integer> progressListener;
        private final int totalSteps;

        public ProgressTask(Callable<T> task, Consumer<Integer> listener, int steps) {
            this.actualTask = task;
            this.progressListener = listener;
            this.totalSteps = steps;
        }

        @Override
        public T call() throws Exception {
            progressListener.accept(0);
            T result = actualTask.call();
            progressListener.accept(100);
            return result;
        }

        public void updateProgress(int step) {
            int percent = (int) ((step / (float) totalSteps) * 100);
            progressListener.accept(percent);
        }
    }

    // Retry mechanism
    public static class RetryPolicy {
        private final int maxAttempts;
        private final long delayMs;
        private final Class<? extends Exception>[] retryableExceptions;

        @SafeVarargs
        public RetryPolicy(int maxAttempts, long delayMs, Class<? extends Exception>... retryableExceptions) {
            this.maxAttempts = maxAttempts;
            this.delayMs = delayMs;
            this.retryableExceptions = retryableExceptions;
        }

        public <T> T execute(Callable<T> task) throws Exception {
            int attempts = 0;
            while (true) {
                try {
                    return task.call();
                } catch (Exception e) {
                    if (!shouldRetry(e) || ++attempts >= maxAttempts) {
                        throw e;
                    }
                    Thread.sleep(delayMs);
                }
            }
        }

        private boolean shouldRetry(Exception e) {
            if (retryableExceptions.length == 0) return true;
            for (Class<? extends Exception> exType : retryableExceptions) {
                if (exType.isInstance(e)) return true;
            }
            return false;
        }
    }

    // Submit with retry policy
    public <T> Future<T> submitWithRetry(Callable<T> task, RetryPolicy retryPolicy, Priority priority) {
        return submit(() -> retryPolicy.execute(task), priority);
    }

    // Get result with timeout
    public <T> Optional<T> getResult(Future<T> future, long timeout, TimeUnit unit)
            throws InterruptedException {
        try {
            return Optional.ofNullable(future.get(timeout, unit));
        } catch (ExecutionException e) {
            return Optional.empty();
        } catch (TimeoutException e) {
            future.cancel(true);
            return Optional.empty();
        }
    }

    // Progress tracking submission
    public <T> Future<T> submitWithProgress(
            Callable<T> task,
            Consumer<Integer> progressListener,
            int totalSteps,
            Priority priority
    ) {
        ProgressTask<T> progressTask = new ProgressTask<>(task, progressListener, totalSteps);
        return submit(progressTask, priority);
    }

    // Graceful shutdown
    public void shutdown(long timeout, TimeUnit unit) throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(timeout, unit);
    }

    // Forceful shutdown
    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }
}