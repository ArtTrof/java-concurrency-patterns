package app.features.executors;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Create a class TaskScheduler that uses different executors for different purposes:
 * <p>
 * Run analytics task every 3 seconds using ScheduledExecutorService.
 * <p>
 * Run heavy batch job on fixed pool of 4 threads.
 * <p>
 * Run async logging with CachedThreadPool.
 * <p>
 * Run a one-time UUID generation job, then shut down.
 * <p>
 * ☑️ Requirements:
 * Use all 4 executors in a single main() method.
 * <p>
 * Use at least 1 Callable (returning UUID)
 * <p>
 * Ensure all pools shut down properly with awaitTermination()
 * <p>
 * Add Thread.sleep(5000) at end of main() to allow scheduled tasks to fire a few times
 */
public class UsingExecutorsTask {

    private static class TaskScheduler {
        public void runScheduledAnalyticTask(ScheduledExecutorService executor) {
            executor.scheduleAtFixedRate(() -> System.out.println("Running 3s scheduled task"), 0, 3, TimeUnit.SECONDS);
        }

        public void runBatchOperation(ExecutorService executor) {
            for (int i = 0; i < 10; i++) {
                executor.execute(() -> {
                    System.out.println("Running batch");
                });

            }
        }

        public void runAsyncLogging(ExecutorService executor) {
            executor.execute(() -> System.out.println("Running async logging task"));
        }

        public Future<UUID> getFutureUUID(ExecutorService executor) {
            Callable<UUID> callable = UUID::randomUUID;
            return executor.submit(callable);
        }
    }

    public static void main(String[] args) {
        var scheduledExecutor = Executors.newScheduledThreadPool(5);
        var fixedExecutor = Executors.newFixedThreadPool(5);
        var cachedExecutor = Executors.newCachedThreadPool();
        var singleExecutor = Executors.newSingleThreadExecutor();
        var scheduler = new TaskScheduler();
        scheduler.runScheduledAnalyticTask(scheduledExecutor);
        for (int i = 0; i < 10; i++) {
            scheduler.runBatchOperation(fixedExecutor);
            scheduler.runAsyncLogging(cachedExecutor);
            Future<UUID> futureUUID = scheduler.getFutureUUID(singleExecutor);
                try {
                    var uuid = futureUUID.get();
                    System.out.println("getFutureUUID: " + uuid);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
        }

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scheduledExecutor.shutdown();
        fixedExecutor.shutdown();
        cachedExecutor.shutdown();
        singleExecutor.shutdown();

        awaitTermination(singleExecutor, 7, TimeUnit.SECONDS);
        awaitTermination(fixedExecutor, 7, TimeUnit.SECONDS);
        awaitTermination(cachedExecutor, 7, TimeUnit.SECONDS);
        awaitTermination(scheduledExecutor, 7, TimeUnit.SECONDS);

    }

    private static void awaitTermination(ExecutorService executor, long timeout, TimeUnit unit) {
        try {
            executor.awaitTermination(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
