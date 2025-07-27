package app.features.futures;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ✅ Task 1: Manual Execution of FutureTask
 * Goal: Understand that FutureTask can run without an executor.
 * <p>
 * Steps:
 * <p>
 * Create a FutureTask<String> with a simple Callable.
 * <p>
 * Start it using a new Thread(futureTask).start().
 * <p>
 * Call get() and print the result.
 * <p>
 * ✅ Task 2: Timeout Handling
 * Goal: Explore behavior when task exceeds timeout.
 * <p>
 * Steps:
 * <p>
 * Create a Callable<Integer> that sleeps for a random time (1–3 sec).
 * <p>
 * Wrap in a FutureTask.
 * <p>
 * Submit to executor.
 * <p>
 * Call get(1, TimeUnit.SECONDS) and handle TimeoutException.
 * <p>
 * ✅ Task 3: Use FutureTask as Latch (Wait for Completion)
 * Goal: Use FutureTask to block main thread until worker completes.
 * <p>
 * Steps:
 * <p>
 * Create a heavy computation task in a FutureTask (e.g. factorial of 10^5).
 * <p>
 * Submit it to a thread.
 * <p>
 * Meanwhile, print "Waiting..." in main.
 * <p>
 * After result is ready, print the result and time taken.
 */
public class UsingFutureTasksTask {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Callable<String> callable = () -> {
            Thread.sleep(1000);
            return "Wow it's working";
        };
        FutureTask<String> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();
        try {
            System.out.println("Task result:" + futureTask.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        Callable<String> callable2 = () -> {
            Thread.sleep(2500);
            return "It won't be seen";
        };
        FutureTask<String> futureTask2 = new FutureTask<>(callable2);
        new Thread(futureTask2).start();
        try {
            var result = futureTask2.get(1, TimeUnit.SECONDS);
            System.out.println("Value is " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("Task timed out");
        }

        Callable<Long> longCallable = () -> factorialUsingRecursion(1000);

        FutureTask<Long> futureTask3 = new FutureTask<>(longCallable);
        new Thread(futureTask3).start();
        checkResult(futureTask3);
    }

    private static long factorialUsingRecursion(int n) throws InterruptedException {
        if (n <= 2) {
            return n;
        }
        return n * factorialUsingRecursion(n - 1);
    }

    private static void checkResult(FutureTask<Long> futureTask) throws InterruptedException, ExecutionException {
        if (!futureTask.isDone()) {
            System.out.println("Waiting for task to end");
            Thread.sleep(250);
            checkResult(futureTask);
        } else {
            futureTask.get();
            System.out.println("Task is completed");
        }
    }
}
