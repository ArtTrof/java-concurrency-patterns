package app.patterns.condition_queues.task;

import java.util.LinkedList;
import java.util.Queue;

public class TaskProcessor {
    private final Queue<Runnable> taskQueue = new LinkedList<>();
    private boolean shutdown = false;
    private final Object lock = new Object();
    private final int MAX_WORKERS = 5;
    private int activeWorkers = 0;

    public TaskProcessor() {
        // Initialize workers
        for (int i = 0; i < MAX_WORKERS; i++) {
            new Worker().start();
        }
    }

    public void addTask(Runnable task) {
        synchronized (lock) {
            taskQueue.add(task);
            lock.notifyAll(); // Notify waiting workers
        }
    }

    public void shutdown() {
        synchronized (lock) {
            shutdown = true;
            lock.notifyAll(); // Wake all workers to check shutdown
        }
    }

    private class Worker extends Thread {
        public void run() {
            while (true) {
                Runnable task;
                synchronized (lock) {
                    // Wait for task or shutdown
                    while (taskQueue.isEmpty() && !shutdown) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

                    if (shutdown && taskQueue.isEmpty()) {
                        return; // Exit on shutdown
                    }

                    task = taskQueue.poll();
                }

                // Run task outside synchronized block
                if (task != null) {
                    task.run();
                }
            }
        }
    }
}