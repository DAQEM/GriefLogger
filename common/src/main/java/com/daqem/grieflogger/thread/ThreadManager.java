package com.daqem.grieflogger.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


public class ThreadManager {

    private static final ExecutorService executor = Executors.newCachedThreadPool(new GriefLoggerThreadFactory());
    private static final Map<Future<?>, OnComplete<?>> onCompleteMap = new HashMap<>();

    public static void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public static <T> void submit(Callable<T> task, OnComplete<T> onComplete) {
        Future<T> future = executor.submit(task);
        onCompleteMap.put(future, onComplete);
    }

    public static <T> Map<Future<T>, OnComplete<T>> getAndRemoveCompleted() {
        //noinspection unchecked
        Map<Future<T>, OnComplete<T>> completedFutures = onCompleteMap.entrySet().stream()
                .filter(entry -> entry.getKey().isDone())
                .collect(Collectors.toMap(
                        entry -> (Future<T>) entry.getKey(),
                        entry -> (OnComplete<T>) entry.getValue()
                ));
        completedFutures.forEach(onCompleteMap::remove);
        return completedFutures;
    }
}
