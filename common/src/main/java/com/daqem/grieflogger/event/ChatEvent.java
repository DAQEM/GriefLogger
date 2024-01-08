package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.ChatService;
import dev.architectury.event.EventResult;

public class ChatEvent {

    public static void registerEvent() {
        dev.architectury.event.events.common.ChatEvent.RECEIVED.register((player, component) -> {
            if (player != null) {
                ChatService chatService = new ChatService(GriefLogger.getDatabase());
                chatService.insertAsync(
                        player.getUUID(),
                        player.level(),
                        player.getOnPos(),
                        component.getString()
                );
            }
            return EventResult.pass();
        });
    }
}
