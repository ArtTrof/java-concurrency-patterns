package app.patterns.thread_safe.publishing.task;

import java.util.concurrent.atomic.AtomicReference;

public class ConfigPublisher {
    private volatile AppConfig appConfig = new AppConfig("app-id", 1);

    public void updateAppConfig(AppConfig appConfig) {
        this.appConfig = new AppConfig(appConfig.appId, appConfig.version);
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    record AppConfig(String appId, int version) {
    }
}

//or
//
//public class ConfigPublisher {
//    private final AtomicReference<AppConfig> configRef =
//            new AtomicReference<>(new AppConfig("default-id", 1));
//
//    // Thread-safe update with validation
//    public void updateAppConfig(String appId, int version) {
//        AppConfig newConfig = new AppConfig(appId, version);
//        configRef.set(newConfig); // Simple atomic update
//
//        // OR for more complex conditional updates:
//        // configRef.updateAndGet(prev -> newConfig);
//    }
//
//    // Atomic get with defensive copy (not needed for records)
//    public AppConfig getAppConfig() {
//        return configRef.get();
//    }
//
//    // Atomic conditional update (example)
//    public boolean updateIfVersionMatches(int expectedVersion, String newAppId) {
//        return configRef.compareAndSet(
//                configRef.get(),
//                new AppConfig(newAppId, expectedVersion + 1)
//        );
//    }
//
//    // Immutable config
//    public record AppConfig(String appId, int version) {
//        public AppConfig {
//            if (appId == null || appId.isBlank()) {
//                throw new IllegalArgumentException("appId cannot be blank");
//            }
//            if (version < 0) {
//                throw new IllegalArgumentException("Version must be positive");
//            }
//        }
//    }
//}
