package app.patterns.shared_state;


import app.patterns.GuardedBy;
import app.patterns.ThreadSafe;

import java.util.concurrent.Executors;


/**
 * Pattern: Protected Shared State
 * <p>
 * Example: A simple Counter example.
 */
@ThreadSafe
public class VisitCounter {

    @GuardedBy("this")
    private int value;

    public synchronized int actualValue() {
        return value;
    }

    public synchronized void increase() {
        value++;
    }

    public synchronized void decrease() {
        value--;
    }

    public static void main(String[] args) {
        var counter = new VisitCounter();
        var threadPool = Executors.newCachedThreadPool();
        for (int i = 1; i <= 50; i++) {
            System.out.println("value " + counter.actualValue() + " i " + i);
            threadPool.execute(() -> counter.increase());
        }
        threadPool.shutdown();
        System.out.println(counter.actualValue());
    }
}
