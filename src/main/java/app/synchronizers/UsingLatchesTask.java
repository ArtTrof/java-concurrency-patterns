package app.synchronizers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Create a class StartupLatchTask with:
 * <p>
 * A CountDownLatch of 5
 * <p>
 * Launch 5 services using ExecutorService (sleep 1s, then print and countDown)
 * <p>
 * Main thread waits on the latch with await()
 * <p>
 * Print "System is fully initialized" after all services are up
 * <p>
 * Bonus:
 * <p>
 * Add 3 extra services that do not affect the latch
 * <p>
 * Show that latch is triggered by the first 5 only
 */
public class UsingLatchesTask {
    public static void main(String[] args) {
        var executors = Executors.newFixedThreadPool(10);
        var latch = new CountDownLatch(5);
        Runnable r = () -> {
            try {
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName() + " is running now");
                latch.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        executors.execute(r);
        executors.execute(r);
        executors.execute(r);
        executors.execute(r);
        executors.execute(r);

        try {
            boolean completed = latch.await(1010, TimeUnit.MILLISECONDS);
            if (completed) {
                System.out.println("All services started successfully");
            } else {
                System.out.println("Timeout occurred before all services started");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Runnable r2 = () -> {
            System.out.println("Non effecting the latch tasks");
        };
        executors.execute(r2);
        executors.execute(r2);
        executors.execute(r2);

        executors.shutdown();
    }
}
