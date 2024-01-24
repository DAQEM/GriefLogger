package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.database.service.SessionService;
import com.daqem.grieflogger.database.service.UserService;
import com.daqem.grieflogger.model.action.SessionAction;
import com.mojang.authlib.GameProfile;
import dev.architectury.event.events.common.PlayerEvent;

import java.util.UUID;

public class PlayerJoinEvent {

    public static void registerEvent() {
        PlayerEvent.PLAYER_JOIN.register(player -> {
            GameProfile gameProfile = player.getGameProfile();
            UUID uuid = gameProfile.getId();

            Services.USER.insertOrUpdateNameAsync(
                    uuid,
                    gameProfile.getName()
            );

            Services.SESSION.insertAsync(
                    uuid,
                    player.level(),
                    player.getOnPos(),
                    SessionAction.JOIN
            );
        });
    }
}
