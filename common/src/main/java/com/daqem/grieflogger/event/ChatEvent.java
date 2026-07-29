package com.daqem.grieflogger.event;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.knot.events.EventResult;
import com.daqem.knot.events.EventsService;

public class ChatEvent {

    public static void registerEvent() {
        EventsService.Server.CHAT_RECEIVED.register((player, component) -> {
            if (player != null) {
                Services.CHAT.insert(
                        player.getUUID(),
                        player.level(),
                        player.getOnPos(),
                        component.getString()
                );
            }
            return EventResult.PASS;
        });
    }
}
