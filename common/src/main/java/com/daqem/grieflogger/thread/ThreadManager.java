package com.daqem.grieflogger.thread;

import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ThreadManager {

    private static final ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new GriefLoggerThreadFactory()
    );

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            new GriefLoggerThreadFactory()
    );

    private static final Map<Future<?>, OnComplete<?>> onCompleteMap = new ConcurrentHashMap<>();

    public static void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public static void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public static <T> void submit(Callable<T> task, OnComplete<T> onComplete) {
        Future<T> future = executor.submit(task);
        onCompleteMap.put(future, onComplete);
    }

    public static <T> Map<Future<T>, OnComplete<T>> getAndRemoveCompleted() {
        Map<Future<T>, OnComplete<T>> completedFutures = onCompleteMap.entrySet().stream()
                .filter(entry -> entry.getKey().isDone())
                .collect(Collectors.toMap(
                        entry -> (Future<T>) entry.getKey(),
                        entry -> (OnComplete<T>) entry.getValue()
                ));
        completedFutures.keySet().forEach(onCompleteMap::remove);
        return completedFutures;
    }

    public static void shutdown() {
        executor.shutdown();
        scheduler.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            scheduler.shutdownNow();
        }
    }
}