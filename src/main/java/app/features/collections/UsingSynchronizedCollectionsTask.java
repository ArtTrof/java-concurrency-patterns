package app.features.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Task:
 * <p>
 * Use a Collections.synchronizedList with a normal ArrayList<Long>.
 * <p>
 * Implement both insertIfAbsent (with synchronized) and insertIfAbsentUnsafe (without).
 * <p>
 * Run 1000 threads and compare the final list size between safe and unsafe versions.
 */
public class UsingSynchronizedCollectionsTask {

    public static void insertIfAbsent(List<Long> list, Long value) {
        synchronized (list) {
            boolean contains = list.contains(value);
            if (!contains) {
                list.add(value);
            }
        }
    }

    public static void insertIfAbsentUnsafe(List<Long> list, Long value) {
        boolean contains = list.contains(value);
        if (!contains) {
            list.add(value);
        }
    }

    public static Set<Long> findDuplicates(List<Long> list) {
        Set<Long> seen = new HashSet<>();
        Set<Long> duplicates = new HashSet<>();
        for (Long value : list) {
            if (!seen.add(value)) {
                duplicates.add(value);
            }
        }
        return duplicates;
    }

    public static void main(String[] args) throws InterruptedException {
        var executor = Executors.newCachedThreadPool();
        List<Long> synchronizedList = Collections.synchronizedList(new ArrayList<>());
        Runnable insertIfAbsent = () -> {
            long value = new Random().nextInt(500);
            insertIfAbsent(synchronizedList, value);
        };
        Runnable insertIfAbsentUnsafe = () -> {
            long value = new Random().nextInt(500);
            insertIfAbsentUnsafe(synchronizedList, value);
        };
        for (int i = 0; i < 1000; i++) {
            executor.execute(insertIfAbsent);
        }
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("Size after safe run:" + synchronizedList.size());
        synchronizedList.clear();

        executor = Executors.newCachedThreadPool();

        for (int i = 0; i < 20000; i++) {
            executor.execute(insertIfAbsentUnsafe);
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("Size after safe unsafe run:" + synchronizedList.size());
        System.out.println("Unsafe duplicates " + findDuplicates(synchronizedList));
    }
}
