package app.features.locks;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Write a class ConfigStore:
 * <p>
 * Field: Map<String, String> config
 * <p>
 * Method: String get(String key) → guarded with readLock
 * <p>
 * Method: void put(String key, String value) → guarded with writeLock
 * <p>
 * Simulate 50 concurrent reads and 5 writes using ExecutorService
 * <p>
 * At the end, print all final keys and values
 */
public class UsingExplicitReadWriteLocksTask {
    private static class ConfigStore {
        private final Map<String, String> config = new ConcurrentHashMap<>();
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        public String get(String key) {
            readWriteLock.readLock().lock();
            try {
                System.out.println("Thread: " + Thread.currentThread().getName()+" is getting read lock");
                return config.get(key);
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        public void set(String key, String value) {
            readWriteLock.writeLock().lock();
            try {
                System.err.println("Setting key" + key + " ,value " + value+ " \n Thread: " + Thread.currentThread().getName());
                config.put(key, value);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }

        {
            config.put("1","1");
            config.put("2","2");
        }
    }

    public static void main(String[] args) {
        var executors = Executors.newFixedThreadPool(10);
        var configStore = new ConfigStore();

        for (int i = 0; i < 50; i++) {
            executors.execute(()->{
                configStore.get("1");
            });
        }

        for (int i = 0; i < 5; i++) {
            executors.execute(()->{
                Integer integer = new Random().nextInt();
                configStore.set(String.valueOf(integer),String.valueOf(integer));
            });
        }

        executors.shutdown();
    }
}
