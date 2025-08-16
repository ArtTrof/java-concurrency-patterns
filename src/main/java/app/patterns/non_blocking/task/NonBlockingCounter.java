package app.patterns.non_blocking.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NonBlockingCounter {
    private AtomicInteger ref = new AtomicInteger(0);

    public void increment() {
        int oldVal, newVal;
        do {
            oldVal = ref.get();
            newVal = oldVal + 1;
        } while (!ref.compareAndSet(oldVal, newVal));
    }

    public Integer getValue() {
        return ref.get();
    }

    public static void main(String[] args) throws InterruptedException {
        var nonBlockingCounter = new NonBlockingCounter();
        ExecutorService executors = Executors.newFixedThreadPool(10);

        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                nonBlockingCounter.increment();
            }
        };

        for (int i = 0; i < 10; i++) {
            executors.submit(task);
        }

        executors.shutdown();
        executors.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Counter: " + nonBlockingCounter.getValue());
    }

}
