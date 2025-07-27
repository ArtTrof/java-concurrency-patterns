package app.features.collections;

import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Goal: Simulate high concurrency and compare behavior of ConcurrentHashMap and CopyOnWriteArrayList.
 * <p>
 * Implement:
 * <p>
 * Map<UUID, Integer> (ConcurrentHashMap)
 * <p>
 * Simulate putIfAbsent in 1000 threads.
 * <p>
 * Count number of unique values after all writes.
 * <p>
 * CopyOnWriteArrayList<Integer>
 * <p>
 * Add elements from 1000 threads (some duplicates allowed).
 * <p>
 * Count total and unique size.
 * <p>
 * Print results after both executions.
 * <p>
 * Optionally, use a Callable that returns partial stats.
 */
public class UsingConcurrentCollectionsTask {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var random = new Random();
        var syncedMap = new ConcurrentHashMap<UUID, Integer>();
        var cOwList = new CopyOnWriteArrayList<Integer>();
        var executor = Executors.newCachedThreadPool();
        var singleThreadExecutor = Executors.newSingleThreadExecutor();


        Callable<Integer> countMapSize = () -> {
            return syncedMap.size();
        };

        Callable<Tuple2<Integer, Integer>> countArraySize = () -> {
            Set<Integer> uniqueSet = new HashSet<>();
            cOwList.forEach(i -> uniqueSet.add(i));
            return Tuple.of(uniqueSet.size(), cOwList.size());
        };

        for (int i = 0; i < 1000; i++) {
            executor.execute(() -> {
                syncedMap.putIfAbsent(UUID.randomUUID(), random.nextInt(10));
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Future<Integer> countedMapSize = singleThreadExecutor.submit(countMapSize);
        System.out.println("Counted Map Size: " + countedMapSize.get());
        singleThreadExecutor.shutdown();

        executor = Executors.newCachedThreadPool();
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        for (int i = 0; i < 1000; i++) {
            executor.execute(() -> {
                cOwList.add(random.nextInt(5000));
            });
        }
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        Future<Tuple2<Integer, Integer>> countedList = singleThreadExecutor.submit(countArraySize);
        System.out.println("Unique list values: " + countedList.get()._1);
        System.out.println("All list values: " + countedList.get()._2);
        singleThreadExecutor.shutdown();
    }
}
