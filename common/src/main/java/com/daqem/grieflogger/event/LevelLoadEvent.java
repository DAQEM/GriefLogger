package com.daqem.grieflogger.event;

import com.daqem.grieflogger.database.service.Services;
import dev.architectury.event.events.common.LifecycleEvent;

public class LevelLoadEvent {

    public static void registerEvent() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level ->
                Services.LEVEL.insertAsync(level.dimension().location().toString()));
    }
}
