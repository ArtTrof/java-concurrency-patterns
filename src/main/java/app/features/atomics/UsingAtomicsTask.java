package app.features.atomics;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 1. ✅ Task: Concurrent Increments with Validation
 * Goal: Verify atomic correctness of concurrent increments.
 * <p>
 * Steps:
 * <p>
 * Create AtomicCounter with both increment() and decrement() methods.
 * <p>
 * Launch:
 * <p>
 * 10,000 threads incrementing
 * <p>
 * 5,000 threads decrementing
 * <p>
 * Final result should be 5000.
 * <p>
 * 2. ✅ Task: Simulate CAS Failure
 * Goal: Explore compareAndSet() behavior under contention.
 * <p>
 * Steps:
 * <p>
 * Use AtomicInteger and multiple threads attempting compareAndSet(0, 1)
 * <p>
 * Print how many succeeded vs failed.
 * <p>
 * 3. ✅ Task: Use AtomicReference<String> for Concurrent Message Passing
 * Goal: Simulate lock-free state update.
 * <p>
 * Steps:
 * <p>
 * Create AtomicReference<String> with default value "init"
 * <p>
 * Spawn 10 threads trying to compareAndSet("init", "updated")
 * <p>
 * Print who succeeded, who failed, and the final state.
 * <p>
 * 4. ✅ Task: Use AtomicMarkableReference<Boolean>
 * Goal: Show optimistic locking + marking.
 * <p>
 * Steps:
 * <p>
 * Use a boolean state + mark (e.g., "loaded" and true)
 * <p>
 * Have threads try to flip the mark from true → false if state is "loaded"
 */
public class UsingAtomicsTask {
    private static class AtomicCounter {
        private AtomicInteger counter = new AtomicInteger(0);

        public void increment() {
            counter.incrementAndGet();
        }

        public void decrement() {
            counter.decrementAndGet();
        }

        public int get() {
            return counter.get();
        }
    }

    public static void main(String[] args) {
        //task1
        var executor = Executors.newCachedThreadPool();
        var counter = new AtomicCounter();
        for (int i = 0; i < 10000; i++) {
            executor.execute(counter::increment);
            if (i % 2 == 0)
                executor.execute(counter::decrement);
        }
        System.out.println("Counter: " + counter.get());
        executor.shutdown();
        System.out.println("\n\n");
        //task2
        var aInteger = new AtomicInteger(0);
        executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            executor.execute(() -> {
                if (aInteger.compareAndSet(0, 1)) {
                    System.out.println("Success aInteger update");
                } else {
                    System.out.println("Failed");
                }
            });
        }
        executor.shutdown();
        System.out.println("\n\n");
        //task 3
        var aReference = new AtomicReference<String>("init");
        executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                if (aReference.compareAndSet("init", "udpated")) {
                    System.out.println("Success ref update");
                } else {
                    System.out.println("Failed ref update");
                }
            });
        }
        executor.shutdown();
        System.out.println("\n\n");
        //task4
        var aMarkableReference = new AtomicMarkableReference<String>("loaded", true);
        executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 200; i++) {
            boolean[] markHolder = new boolean[1];
            String ref = aMarkableReference.get(markHolder);
            boolean currentMark = markHolder[0];

            boolean updated = aMarkableReference.compareAndSet(ref, ref, currentMark, !currentMark);
            if (updated) {
                System.out.println("Successfully flipped mark to " + !currentMark);
            }

        }
        executor.shutdown();
    }
}
