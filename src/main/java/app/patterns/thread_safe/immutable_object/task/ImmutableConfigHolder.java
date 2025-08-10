package app.patterns.thread_safe.immutable_object.task;

import lombok.AllArgsConstructor;
import lombok.Value;

public class ImmutableConfigHolder {
    private volatile AppConfig appConfig = new AppConfig(0, 0, false);

    public void updateAppConfig(int timeout, int retryCount, boolean loggingEnabled) {
        this.appConfig = new AppConfig(timeout, retryCount, loggingEnabled);
    }

    public AppConfig getAppConfig() {
        return this.appConfig;
    }

    @Value
    @AllArgsConstructor
    static class AppConfig {
        int timeout;
        int retryCount;
        boolean loggingEnabled;
    }
    //or
//    final record AppConfig(int timeout, int retryCount, boolean loggingEnabled) {
//    }
}
