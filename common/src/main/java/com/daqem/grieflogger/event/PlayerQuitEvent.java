package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.SessionService;
import com.daqem.grieflogger.model.action.SessionAction;
import dev.architectury.event.events.common.PlayerEvent;

public class PlayerQuitEvent {

    public static void registerEvent() {
        PlayerEvent.PLAYER_QUIT.register(player -> {
            SessionService sessionService = new SessionService(GriefLogger.getDatabase());
            sessionService.insertAsync(
                    player.getUUID(),
                    player.level(),
                    player.getOnPos(),
                    SessionAction.QUIT);
        });
    }
}
