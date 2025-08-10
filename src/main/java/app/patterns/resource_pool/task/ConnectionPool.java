package app.patterns.resource_pool.task;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {
    private final Semaphore semaphore;
    private final BlockingQueue<DbConnection> connections;

    public ConnectionPool(int poolSize, List<DbConnection> initialConnections) {
        this.semaphore = new Semaphore(poolSize, true);
        this.connections = new LinkedBlockingQueue<>(initialConnections);
        this.connections.addAll(initialConnections);
    }


    public DbConnection borrowConnection(long timeout, TimeUnit unit)
            throws InterruptedException {
        semaphore.acquire();
        try {
            DbConnection polled = connections.poll(timeout, unit);
            return polled;
        } finally {
            semaphore.release();
        }
    }

    public DbConnection borrowConnection() throws InterruptedException {
        semaphore.acquire(); // Wait for slot
        return connections.poll(); // Take connection (may block if empty)
        // NO release here! Hold permit until returnConnection()
    }

    public void returnConnection(DbConnection conn) throws InterruptedException {
        if (conn != null) {
            connections.put(conn); // Return to pool
            semaphore.release();   // Free up slot
        }
    }

    public void shutdown() {
        connections.forEach(DbConnection::close);
    }
}
