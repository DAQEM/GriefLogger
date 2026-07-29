package com.daqem.grieflogger.event;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.action.SessionAction;
import com.daqem.knot.events.EventsService;
import com.mojang.authlib.GameProfile;

import java.util.UUID;

public class PlayerJoinEvent {

    public static void registerEvent() {
        EventsService.Player.PLAYER_JOIN.register(player -> {
            GameProfile gameProfile = player.getGameProfile();
            UUID uuid = gameProfile.id();

            Services.USER.insertOrUpdateName(
                    uuid,
                    gameProfile.name()
            );

            Services.SESSION.insert(
                    uuid,
                    player.level(),
                    player.getOnPos(),
                    SessionAction.JOIN
            );
        });
    }
}
