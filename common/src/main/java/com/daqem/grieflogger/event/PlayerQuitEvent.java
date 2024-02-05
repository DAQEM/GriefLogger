package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.database.service.SessionService;
import com.daqem.grieflogger.model.action.SessionAction;
import dev.architectury.event.events.common.PlayerEvent;

public class PlayerQuitEvent {

    public static void registerEvent() {
        PlayerEvent.PLAYER_QUIT.register(player ->
                Services.SESSION.insertAsync(
                        player.getUUID(),
                        player.getLevel(),
                        player.getOnPos(),
                        SessionAction.QUIT));
    }
}
