package app.locks;

import java.util.concurrent.Executors;

/**
 * Create a class Counter with:
 * <p>
 * A field count.
 * <p>
 * A method increment() synchronized on this.
 * <p>
 * Use ExecutorService to run 1000 increments in parallel.
 * <p>
 * At the end, print the final count — it must be 1000.
 */
public class UsingIntrinsicLocksTask {
    private static class Counter {
        private int count;

        public void increment() {
            synchronized (this) {
                System.out.println("Incrementing thread:" + Thread.currentThread().getName());
                count++;
            }
        }
        //same as this
//        public synchronized void increment() {
//                System.out.println("Incrementing thread:" + Thread.currentThread().getName());
//                count++;
//        }
        public synchronized int getCount() {
            return count;
        }
    }

    public static void main(String[] args) {
        var executor = Executors.newCachedThreadPool();
        var syncedCounter = new Counter();

        for (int i = 0; i < 10000; i++) {
            executor.execute(()->{
                syncedCounter.increment();
                System.out.println(syncedCounter.getCount());
            });
        }
        try {
            executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor.shutdown();
    }
}

