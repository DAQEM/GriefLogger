package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.LevelService;
import dev.architectury.event.events.common.LifecycleEvent;

public class LevelLoadEvent {

    public static void registerEvent() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            LevelService levelService = new LevelService(GriefLogger.getDatabase());
            levelService.insertAsync(level.dimension().location().toString());
        });
    }
}
