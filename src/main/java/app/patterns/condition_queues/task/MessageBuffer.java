package app.patterns.condition_queues.task;

import java.util.concurrent.locks.*;

public class MessageBuffer {
    private final String[] buffer;  // Fixed-size circular buffer
    private int count = 0;          // Current number of messages
    private int putIndex = 0;       // Where to put next message
    private int takeIndex = 0;      // Where to take next message

    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();  // "There's space" signal
    private final Condition notEmpty = lock.newCondition(); // "There's data" signal

    public MessageBuffer(int capacity) {
        this.buffer = new String[capacity];
    }

    // Producer calls this
    public void put(String message) throws InterruptedException {
        lock.lock();
        try {
            // Wait while buffer is full
            while (count == buffer.length) {
                notFull.await();  // "I'll sleep until someone says there's space"
            }

            // Put message in buffer
            buffer[putIndex] = message;
            putIndex = (putIndex + 1) % buffer.length; // Wrap around
            count++;

            // Wake up any waiting consumers
            notEmpty.signal(); // "Hey consumers, there's data now!"
        } finally {
            lock.unlock();
        }
    }

    // Consumer calls this
    public String take() throws InterruptedException {
        lock.lock();
        try {
            // Wait while buffer is empty
            while (count == 0) {
                notEmpty.await(); // "I'll sleep until someone says there's data"
            }

            // Take message from buffer
            String message = buffer[takeIndex];
            buffer[takeIndex] = null; // Clear slot
            takeIndex = (takeIndex + 1) % buffer.length; // Wrap around
            count--;

            // Wake up any waiting producers
            notFull.signal(); // "Hey producers, there's space now!"
            return message;
        } finally {
            lock.unlock();
        }
    }
}