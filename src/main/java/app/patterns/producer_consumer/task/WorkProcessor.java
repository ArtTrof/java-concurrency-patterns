package app.patterns.producer_consumer.task;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkProcessor {
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final AtomicInteger operationsCounter = new AtomicInteger(0);
    private final ExecutorService executor;
    private volatile boolean running = true;

    public WorkProcessor() {
        this.executor = Executors.newFixedThreadPool(5);
    }

    public void start(int producerCount, int consumerCount) {
        // Start producers
        for (int i = 0; i < producerCount; i++) {
            executor.submit(this::produce);
        }

        // Start consumers
        for (int i = 0; i < consumerCount; i++) {
            executor.submit(this::consume);
        }
    }

    private void produce() {
        try {
            while (running) {
                String item = UUID.randomUUID().toString();
                queue.put(item);
                // Slow down producers for demonstration
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void consume() {
        try {
            while (running) {
                String item = queue.poll(100, TimeUnit.MILLISECONDS);
                if (item != null) {
                    operationsCounter.incrementAndGet();
                    System.out.println(Thread.currentThread().getName() + " processed: " + item);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public int getProcessedCount() {
        return operationsCounter.get();
    }

    public static void main(String[] args) {
        WorkProcessor processor = new WorkProcessor();

        try {
            processor.start(2, 3); // 2 producers, 3 consumers

            // Let it run for 5 seconds
            Thread.sleep(5000);

            System.out.println("Total processed: " + processor.getProcessedCount());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            processor.shutdown();
        }
    }
}