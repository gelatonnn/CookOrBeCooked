package utils;

import java.util.concurrent.*;

public class TimerUtils {
    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(20);

    public static ScheduledFuture<?> schedule(Runnable r, long delayMs) {
        return scheduler.schedule(r, delayMs, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture<?> scheduleSeconds(Runnable r, long seconds) {
        return scheduler.schedule(r, seconds, TimeUnit.SECONDS);
    }

    public static ScheduledFuture<?> repeat(Runnable r, long periodMs) {
        return scheduler.scheduleAtFixedRate(r, 0, periodMs, TimeUnit.MILLISECONDS);
    }

    public static void shutdown() {
        scheduler.shutdownNow();
    }
}