package com.daqem.grieflogger.event;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.action.SessionAction;
import com.daqem.knot.events.EventsService;

public class PlayerQuitEvent {

    public static void registerEvent() {
        EventsService.Player.PLAYER_QUIT.register(player -> {
            Services.SESSION.insert(
                    player.getUUID(),
                    player.level(),
                    player.getOnPos(),
                    SessionAction.QUIT);
        });
    }
}
