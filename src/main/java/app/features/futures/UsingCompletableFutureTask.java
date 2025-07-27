package app.features.futures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * ✅ Basic Usage
 * supplyAsync + thenApply
 * <p>
 * Supply a random UUID
 * <p>
 * Convert to uppercase using thenApply
 * <p>
 * Print result
 * <p>
 * runAsync + thenRun
 * <p>
 * Run a task that sleeps and prints "Start"
 * <p>
 * Chain thenRun that prints "End"
 * <p>
 * thenAccept
 * <p>
 * Generate a number, then accept and print it with prefix "Generated: "
 * <p>
 * ✅ Chaining and Composition
 * thenCompose (flatMap style)
 * <p>
 * First supply random username
 * <p>
 * Then chain another call to get profile data (simulated)
 * <p>
 * thenCombine
 * <p>
 * Combine two random ints and return the sum
 * <p>
 * thenAcceptBoth
 * <p>
 * Accept two ints and print both
 * <p>
 * ✅ Coordination
 * anyOf
 * <p>
 * 3 suppliers with different sleep times
 * <p>
 * Print "First completed: X"
 * <p>
 * allOf
 * <p>
 * Wait for 3 slow tasks
 * <p>
 * After all complete, print "All done"
 * <p>
 * ✅ Exception Handling
 * exceptionally
 * <p>
 * Create a task that throws
 * <p>
 * Handle via exceptionally and return fallback
 * <p>
 * handle
 * <p>
 * Create task that throws sometimes
 * <p>
 * Use handle to differentiate success/failure
 * <p>
 * ✅ Manual Completion
 * Manually complete
 * <p>
 * Create empty CompletableFuture<String>
 * <p>
 * Complete it with "manual" after 500ms
 * <p>
 * Wait on it in main thread
 * <p>
 * ✅ Timeout Simulation
 * Timeout race
 * <p>
 * Use completeOnTimeout(...) (Java 9+), or simulate:
 * <p>
 * If task doesn't complete in X ms, cancel or return fallback
 * <p>
 * ✅ Combine with Executor
 * Custom thread pool
 * <p>
 * Supply UUIDs using supplyAsync(..., executor)
 * <p>
 * Run 5 tasks and print thread names
 * <p>
 * ✅ Real-World Simulation
 * Parallel API calls
 * <p>
 * 3 fake APIs (random delays, return strings)
 * <p>
 * Wait for all and collect results into a list
 * <p>
 * Service fallback
 * <p>
 * Call fast & slow versions of a function using acceptEither
 */
public class UsingCompletableFutureTask {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //task 1
        var executor = Executors.newCachedThreadPool();
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> UUID.randomUUID().toString());
        String upperUUID = cf.thenApplyAsync(String::toUpperCase, executor).get();
        System.out.println("Task 1 res: " + upperUUID + "\n");

        //task 2
        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() -> {
            System.out.println("Start");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        cf2.thenRunAsync(() -> {
            System.out.println("End \n");
        }, executor).get();

        //task 3
        CompletableFuture<String> cf3 = CompletableFuture.supplyAsync(() -> {
            return String.valueOf(new Random().nextInt());
        });
        cf3.thenAcceptAsync(str -> {
            System.out.println("Generated:" + str + "\n");
        }, executor);

        //task 4
        CompletableFuture<Map<String, String>> cf4 = CompletableFuture.supplyAsync(() -> Map.of("Firstname", "Lastname"));
        cf4.thenComposeAsync(map -> CompletableFuture.supplyAsync(() -> "Lastname is " + map.get("Firstname")), executor).thenAccept(System.out::println);

        //task 5
        CompletableFuture<UUID> cf5a = CompletableFuture.supplyAsync(UUID::randomUUID);
        CompletableFuture<UUID> cf5b = CompletableFuture.supplyAsync(UUID::randomUUID);
        cf5a.thenCombineAsync(cf5b, (first, second) -> {
            var num1 = Integer.valueOf(first.hashCode());
            var num2 = Integer.valueOf(first.hashCode());
            return num1 + num2;
        }).thenAcceptAsync((res) -> {
            System.out.println("\nSum of uuid hashes is:" + res + "\n");
        }, executor);

        //task6
        CompletableFuture<Integer> cf6a = CompletableFuture.supplyAsync(() -> new Random().nextInt());
        CompletableFuture<Integer> cf6b = CompletableFuture.supplyAsync(() -> new Random().nextInt());
        cf6b.thenAcceptBothAsync(cf6a, (n1, n2) -> {
            System.out.println("N1 " + n1);
            System.out.println("N2 " + n2);
            System.out.println("\n");
        }, executor);

        //task 7
        CompletableFuture<String> cf7S1 = randomDelayFuture.apply("First");
        CompletableFuture<String> cf7S2 = randomDelayFuture.apply("Second");
        CompletableFuture<String> cf7S3 = randomDelayFuture.apply("Third");
        CompletableFuture.anyOf(cf7S1, cf7S2, cf7S3).thenAcceptAsync((r) -> {
            System.out.println("Wins: " + r + "\n");
        }, executor);

        //task 8
        CompletableFuture.allOf(cf7S1, cf7S2, cf7S3).thenAcceptAsync((r) -> {
            System.out.println("All three completed\n");
        }, executor).join();  // Block until all complete;

        //task9
        CompletableFuture<Void> cf9 = CompletableFuture.runAsync(() -> {
            throw new RuntimeException("Thrown exception");
        });
        CompletableFuture.allOf(cf9).exceptionallyAsync((throwable) -> {
            System.out.printf("Exception: %s\n", throwable.getMessage());
            return null;
        }, executor);

        //task 10
        CompletableFuture<String> cf10Throwing = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Thrown exception");
        });
        CompletableFuture<String> cf10 = CompletableFuture.supplyAsync(() -> "OkValue");
        CompletableFuture.allOf(cf10Throwing, cf10).handleAsync((res, ex) -> {
            if (ex != null) {
                System.out.printf("Exception occurred: %s\n", ex.getMessage());
                return "FallbackValue";
            }
            return res;
        }, executor).thenAcceptAsync(res -> {
            System.out.printf("Final result: %s", res + "\n");
        }, executor);

        //task 11
        // Fast completion case (usually shows "Delayed")
        CompletableFuture<String> fast = CompletableFuture.supplyAsync(() -> "Delayed");
        Thread.sleep(500);
        fast.complete("ok");
        System.out.println("Fast: " + fast.get()); // Usually "Delayed"
        // Slow completion case (will show "ok")
        CompletableFuture<String> slow = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Delayed";
        });
        Thread.sleep(500);
        slow.complete("ok");
        System.out.println("Slow: " + slow.get()); // Always "ok"

        //task 12
        CompletableFuture<String> slow2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Delayed";
        });
        slow2.completeOnTimeout("\nCompleted due to time out", 250, TimeUnit.MILLISECONDS);
        System.out.println(slow2.get());

        //task 13
        CompletableFuture<UUID> cf13a = CompletableFuture.supplyAsync(UUID::randomUUID, executor).whenCompleteAsync((res, ex) -> {
            System.out.println("Supplied by thread " + Thread.currentThread().getName());
        });
        CompletableFuture<UUID> cf13b = CompletableFuture.supplyAsync(UUID::randomUUID, executor).whenCompleteAsync((res, ex) -> {
            System.out.println("Supplied by thread " + Thread.currentThread().getName());
        });
        CompletableFuture<UUID> cf13c = CompletableFuture.supplyAsync(UUID::randomUUID, executor).whenCompleteAsync((res, ex) -> {
            System.out.println("Supplied by thread " + Thread.currentThread().getName());
        });
        CompletableFuture<UUID> cf13d = CompletableFuture.supplyAsync(UUID::randomUUID, executor).whenCompleteAsync((res, ex) -> {
            System.out.println("Supplied by thread " + Thread.currentThread().getName());
        });
        CompletableFuture<UUID> cf13e = CompletableFuture.supplyAsync(UUID::randomUUID, executor).whenCompleteAsync((res, ex) -> {
            System.out.println("Supplied by thread " + Thread.currentThread().getName());
        });
        CompletableFuture.allOf(cf13a, cf13b, cf13c, cf13d, cf13e).join();

        //task 14

        CompletableFuture<String> cf14a = delayedCallSimulation(executor);
        CompletableFuture<String> cf14b = delayedCallSimulation(executor);
        CompletableFuture<String> cf14c = delayedCallSimulation(executor);

        CompletableFuture.allOf(cf14a, cf14b, cf14c).join();
        List<String> results = List.of(
                cf14a.join(),
                cf14b.join(),
                cf14c.join()
        );
        results.forEach(System.out::println);

        //or
//        CompletableFuture.allOf(cf14a, cf14b, cf14c)
//                .thenRun(() -> {
//                    System.out.println("Result A: " + cf14a.join());
//                    System.out.println("Result B: " + cf14b.join());
//                    System.out.println("Result C: " + cf14c.join());
//                })
//                .join();

        //also
//        CompletableFuture<String> cf14a = delayedCallSimulation(executor)
//                .thenAcceptAsync(result -> System.out.println("Result A: " + result), executor);
//        CompletableFuture<String> cf14b = delayedCallSimulation(executor)
//                .thenAcceptAsync(result -> System.out.println("Result B: " + result), executor);
//        CompletableFuture<String> cf14c = delayedCallSimulation(executor)
//                .thenAcceptAsync(result -> System.out.println("Result C: " + result), executor);
//        // Wait for all (if needed)
//        CompletableFuture.allOf(cf14a, cf14b, cf14c).join();

        //task 15

        CompletableFuture<String> cf15Slow = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Slooow";
        }, executor);

        CompletableFuture<String> cf15Fast = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Faster";
        }, executor);

        // Block until the first completes, then print
        cf15Fast.acceptEitherAsync(cf15Slow, result -> {
            System.out.println("\nResult: " + result);
        }).join(); // Ensures output appears

        executor.shutdown();
    }

    private static CompletableFuture<String> delayedCallSimulation(ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            var delay = new Random().nextInt(3);
            try {
                Thread.sleep(delay * 999);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Completed after ms:" + (delay * 999);
        }, executor);
    }

    private static final Function<String, CompletableFuture<String>> randomDelayFuture = name ->
            CompletableFuture.supplyAsync(() -> {
                int multiplier = new Random().nextInt(3);
                try {
                    Thread.sleep(multiplier * 1000);
                    return name;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
}
