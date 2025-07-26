package app.synchronizers;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class UsingBarriersTask {

    public static void main(String[] args) {
        final int PHASES = 3;
        final int THREADS_PER_PHASE = 5;

        AtomicInteger phaseCounter = new AtomicInteger(1);

        CyclicBarrier barrier = new CyclicBarrier(
                THREADS_PER_PHASE,
                () -> System.out.println("Phase " + phaseCounter.getAndIncrement() + " completed")
        );

        var executor = Executors.newFixedThreadPool(THREADS_PER_PHASE);

        Runnable task = () -> {
            for (int i = 0; i < PHASES; i++) {
                try {
                    Thread.sleep(new Random().nextInt(500));
                    System.out.println(Thread.currentThread().getName() + " reached barrier for phase " + (i + 1));
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        };

        for (int i = 0; i < THREADS_PER_PHASE; i++) {
            executor.execute(task);
        }

        executor.shutdown();
    }
}

