package com.daqem.grieflogger.event;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.knot.events.EventsService;

import java.util.ArrayList;
import java.util.List;

public class LevelLoadEvent {

    private static final List<String> registeredLevels = new ArrayList<>();

    public static void registerEvent() {
        EventsService.Server.LevelLifecycle.SERVER_LEVEL_LOAD.register(level -> {
            String levelName = level.dimension().identifier().toString();
            if (!registeredLevels.contains(levelName)) {
                registeredLevels.add(levelName);
                Services.LEVEL.insert(level.dimension().identifier().toString());
            }
        });
    }
}
