package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.thread.ThreadManager;
import com.daqem.knot.events.EventsService;

public class ServerStoppedEvent {

    public static void registerEvent() {

        EventsService.Server.LIFECYCLE_STOPPING.register(server -> {
            GriefLogger.LOGGER.info("Stopping GriefLogger threads...");

            Database database = GriefLogger.getDatabase();
            if (database != null) {
                GriefLogger.LOGGER.info("Flushing final database queue...");
                try {
                    database.queue.execute();
                    database.batchQueue.execute();
                } catch (Exception e) {
                    GriefLogger.LOGGER.error("Failed to flush database queue on shutdown", e);
                }
            }

            ThreadManager.shutdown();
        });
    }
}
