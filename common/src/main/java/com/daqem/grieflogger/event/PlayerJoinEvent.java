package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.UserService;
import com.mojang.authlib.GameProfile;
import dev.architectury.event.events.common.PlayerEvent;

public class PlayerJoinEvent {

    public static void registerEvent() {
        PlayerEvent.PLAYER_JOIN.register(player -> {
            UserService userService = new UserService(GriefLogger.getDatabase());
            GameProfile gameProfile = player.getGameProfile();
            userService.insertOrUpdateName(gameProfile.getId(), gameProfile.getName());
        });
    }
}
