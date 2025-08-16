package app.patterns.task_convergence.task;

import java.util.*;
import java.util.concurrent.*;

public class TextAggregator {

    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private final List<Integer> partialCounts = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executor;
    private final CyclicBarrier barrier;
    private final List<String> textChunks;

    public TextAggregator(String textInput) {
        // Split text once, by lines (or any rule you want)
        this.textChunks = Arrays.asList(textInput.split("\\r?\\n"));

        // Barrier convergence: sum after all threads finish
        this.barrier = new CyclicBarrier(textChunks.size(), () -> {
            int total = partialCounts.stream().mapToInt(Integer::intValue).sum();
            System.out.println("Chunks: " + textChunks.size());
            System.out.println("Word counts per chunk: " + partialCounts);
            System.out.println("Total word count: " + total);
        });

        this.executor = Executors.newFixedThreadPool(CORES);
    }

    private Runnable createTask(String chunk) {
        return () -> {
            int count = chunk.split("\\s+").length;
            partialCounts.add(count);
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public void run() throws InterruptedException {
        for (String chunk : textChunks) {
            executor.execute(createTask(chunk));
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        String input = """
                Hello world this is one line
                And here is another line
                Java concurrency is powerful
                But tricky if you miss barriers
                """;
        new TextAggregator(input).run();
    }
}
