package app.features.synchronizers;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class UsingSemaphoresTask {
    public static void main(String[] args) {
        var executor = Executors.newFixedThreadPool(10);
        var semaphore = new Semaphore(2);
        AtomicInteger printed = new AtomicInteger(0);
        AtomicInteger skipped = new AtomicInteger(0);

        Runnable task = () -> {
            boolean acquired = false;
            try {
                System.out.println("Trying to acquire - " + Thread.currentThread().getName());
                acquired = semaphore.tryAcquire(1, TimeUnit.SECONDS);
                if (acquired) {
                    System.out.println("Acquired - " + Thread.currentThread().getName());
                    printed.incrementAndGet();
                    Thread.sleep(1500);
                } else {
                    System.out.println("Skipped - " + Thread.currentThread().getName());
                    skipped.incrementAndGet();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (acquired) semaphore.release();
            }
        };

        for (int i = 0; i < 10; i++) {
            executor.execute(task);
        }
        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.printf("Printed: %d\nSkipped: %d\n", printed.get(), skipped.get());
    }
}
