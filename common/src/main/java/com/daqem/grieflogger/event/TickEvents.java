package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.config.GriefLoggerConfig;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.thread.ThreadManager;
import dev.architectury.event.events.common.TickEvent;

import java.util.concurrent.ExecutionException;

public class TickEvents {

    private static long lastTick = 0;

    public static void registerEvents() {
        TickEvent.SERVER_POST.register(server -> {
            ThreadManager.getAndRemoveCompleted().forEach(
                    (future, onComplete) -> {
                        try {
                            onComplete.onComplete(future.get());
                        } catch (InterruptedException | ExecutionException e) {
                            GriefLogger.LOGGER.error("Error executing task", e);
                        }
                    }
            );
            ThreadManager.execute(() -> {
                if (lastTick % GriefLoggerConfig.queueFrequency.get() == 0) {
                    Database database = GriefLogger.getDatabase();
                    database.queue.execute();
                    database.batchQueue.execute();
                }
            });

            lastTick++;
        });
    }
}
