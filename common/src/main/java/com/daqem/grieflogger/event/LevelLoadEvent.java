package com.daqem.grieflogger.event;

import com.daqem.grieflogger.database.service.Services;
import dev.architectury.event.events.common.LifecycleEvent;

import java.util.ArrayList;
import java.util.List;

public class LevelLoadEvent {

    private static final List<String> registeredLevels = new ArrayList<>();

    public static void registerEvent() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            String levelName = level.dimension().location().toString();
            if (!registeredLevels.contains(levelName)) {
                registeredLevels.add(levelName);
                Services.LEVEL.insertAsync(level.dimension().location().toString());
            }
        });
    }
}
