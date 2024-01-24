package com.daqem.grieflogger.event;

import com.daqem.grieflogger.database.service.Services;
import dev.architectury.event.EventResult;

public class ChatEvent {

    public static void registerEvent() {
        dev.architectury.event.events.common.ChatEvent.RECEIVED.register((player, component) -> {
            if (player != null) {
                Services.CHAT.insertAsync(
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
