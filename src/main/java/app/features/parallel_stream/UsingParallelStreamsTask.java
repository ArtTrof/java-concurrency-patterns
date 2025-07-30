package app.features.parallel_stream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Task 1 — Parallel Prime Count
 * Write a method that:
 * public static long countPrimesParallel(int max)
 * Returns the count of prime numbers from 0 to max, using .parallel().
 * Compare with:
 * public static long countPrimesSequential(int max)*
 * <p>
 * Task 2 — Ordered vs Unordered Output
 * Write two functions that:
 * <p>
 * Print numbers 0–50 in parallel using forEach()
 * <p>
 * Print them in order using forEachOrdered()
 * <p>
 * Compare output.
 * <p>
 * Task 3 — Shared Mutable State Bug
 * Demonstrate the issue below:
 * <p>
 * Create a List<Integer> result = new ArrayList<>();
 * <p>
 * In a parallel stream:
 * .forEach(result::add);
 * <p>
 * ☠ Observe that result.size() may be wrong or even crash.
 * 💡 Then, fix it with Collectors.toList().
 * <p>
 * Task 4 — Parallel Reduce
 * Implement sum using .reduce() with .parallel()
 * <p>
 * IntStream.range(1, 100_000).parallel().reduce(0, Integer::sum)
 * Add benchmark vs .sum().
 * <p>
 * Task 5 — Performance Benchmark Tool
 * Write a generic method:
 * <T> T measure(String label, Supplier<T> task)
 * Use it to benchmark both sequential and parallel stream operations.
 * <p>
 * [Advanced] Task 6 — Custom ForkJoinPool for Stream
 * By default, parallelStream() uses the common pool.
 * Show how to run a parallel stream in a custom ForkJoinPool:
 * ForkJoinPool pool = new ForkJoinPool(4);
 * pool.submit(() -> Stream...parallel()...).get();
 */
public class UsingParallelStreamsTask {
    public static void main(String[] args) {
        //task1
        var counter = new PrimeCounter();
        counter.countPrimesSequential(10_000);
        counter.countPrimesParallel(10_000);

        long start = System.currentTimeMillis();
        long seqCount = counter.countPrimesSequential(100_000);
        long seqTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        long parCount = counter.countPrimesParallel(100_000);
        long parTime = System.currentTimeMillis() - start;

        System.out.println("Sequential count: " + seqCount + " primes");
        System.out.println("Sequential time: " + seqTime + " ms");
        System.out.println("Parallel count: " + parCount + " primes");
        System.out.println("Parallel time: " + parTime + " ms");
        System.out.println("Speedup factor: " + (double) seqTime / parTime + "x");

        //task2
        System.out.println("Unordered parallel:\n");
        IntStream.rangeClosed(0, 50)
                .parallel()
                .forEach(System.out::println);

        System.out.println("Ordered parallel:\n");
        IntStream.rangeClosed(0, 50)
                .parallel()
                .forEachOrdered(System.out::println);

        //task3
        List<Integer> result = new ArrayList<>();
        IntStream.rangeClosed(0, 50_000)
                .parallel()
                .forEach(result::add);  // This will fail randomly
        // The correct parallel collection:
        List<Integer> safeList = IntStream.rangeClosed(0, 50_000)
                .parallel()
                .boxed()
                .toList();
        System.out.println("\nParallel unsafe list size: " + result.size());
        System.out.println("\nSafe list size: " + safeList.size());

        //task4
        int reduceResult = IntStream.range(1, 100_000).parallel().reduce(0, Integer::sum);
        System.out.println("\nParallel reduce result: " + reduceResult);

        //task5
        measure("Paraller prime counter", () -> {
            return counter.countPrimesParallel(2_000_000);
        });

        //task6
        System.out.println("\n");
        ForkJoinPool pool = new ForkJoinPool(4);
        pool.submit(() -> IntStream.rangeClosed(0, 100)
                .parallel()
                .forEach(System.out::println)
        ).join();
        pool.shutdown();

    }

    private static <T> T measure(String label, Supplier<T> task) {
        var start = System.currentTimeMillis();
        var result = task.get();
        long execTime = System.currentTimeMillis() - start;
        System.out.println("Measured " + label + " time: " + execTime + " ms");
        return result;
    }

    static class PrimeCounter {
        public long countPrimesParallel(int max) {
            return IntStream.rangeClosed(2, max)  // Start from 2 (first prime)
                    .parallel()                    // Enable parallel processing
                    .filter(this::isPrime)        // Filter primes
                    .count();
        }

        public long countPrimesSequential(int max) {
            return IntStream.rangeClosed(2, max)
                    .filter(this::isPrime)
                    .count();
        }

        private boolean isPrime(int number) {
            if (number <= 1) return false;
            if (number == 2) return true;
            if (number % 2 == 0) return false;

            // Check divisors up to sqrt(number)
            int maxDivisor = (int) Math.sqrt(number) + 1;
            for (int i = 3; i < maxDivisor; i += 2) {
                if (number % i == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
