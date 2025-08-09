package app.patterns.thread_confinement.task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ThreadLocalConnectionHolder {
    private static final String DB_URL = "jdbc:mysql://localhost/mydb";
    private static final String USER = "user";
    private static final String PASS = "password";

    private static final ThreadLocal<Connection> connectionHolder = ThreadLocal.withInitial(() -> {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException e) {
            throw new ConnectionInitializationException("Failed to initialize connection", e);
        }
    });

    public static Connection getConnection() {
        Connection conn = connectionHolder.get();
        try {
            if (conn == null || conn.isClosed()) {
                connectionHolder.remove();
                conn = connectionHolder.get();
            }
        } catch (SQLException e) {
            throw new ConnectionException("Connection check failed", e);
        }
        return conn;
    }

    public static void closeConnection() {
        Connection conn = connectionHolder.get();
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
                conn.close();
            } catch (SQLException e) {
                throw new ConnectionException("Failed to close connection", e);
            } finally {
                connectionHolder.remove();
            }
        }
    }

    public static class ConnectionException extends RuntimeException {
        public ConnectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ConnectionInitializationException extends ConnectionException {
        public ConnectionInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}