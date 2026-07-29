package com.daqem.grieflogger.event;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.knot.events.EventResult;
import com.daqem.knot.events.EventsService;
import net.minecraft.server.level.ServerPlayer;

public class CommandEvent {

    public static void registerEvent() {
        EventsService.Server.COMMAND_PERFORM.register((results, exception) -> {
            ServerPlayer player = results.get().getContext().getSource().getPlayer();
            if (player != null) {
                Services.COMMAND.insert(
                        player.getUUID(),
                        player.level(),
                        player.getOnPos(),
                        results.get().getReader().getString()
                );
            }
            return EventResult.PASS;
        });
    }
}
