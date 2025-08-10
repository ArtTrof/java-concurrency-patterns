package app.patterns.thread_safe.initialization.task;

public class ServiceLocator {
    // Private holders ensure proper encapsulation
    private static class DatabaseServiceHolder {
        static final DatabaseService INSTANCE = new DatabaseServiceImpl();
    }

    private static class LoggingServiceHolder {
        static final LoggingService INSTANCE = new LoggingServiceImpl();
    }

    public static DatabaseService getDatabaseService() {
        DatabaseService instance = DatabaseServiceHolder.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("DatabaseService not initialized");
        }
        return instance;
    }

    public static LoggingService getLoggingService() {
        LoggingService instance = LoggingServiceHolder.INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("LoggingService not initialized");
        }
        return instance;
    }

    // Concrete implementations (could be in separate files)
    private static class DatabaseServiceImpl implements DatabaseService {
        @Override
        public void connect() {
            System.out.println("Connected to database");
        }
    }

    private static class LoggingServiceImpl implements LoggingService {
        @Override
        public void log(String message) {
            System.out.println("[LOG] " + message);
        }
    }

    public interface DatabaseService {
        void connect();
    }

    public interface LoggingService {
        void log(String message);
    }
}