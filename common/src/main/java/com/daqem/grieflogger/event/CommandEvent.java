package com.daqem.grieflogger.event;

import com.daqem.grieflogger.database.service.Services;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandPerformEvent;
import net.minecraft.server.level.ServerPlayer;

public class CommandEvent {

    public static void registerEvent() {
        CommandPerformEvent.EVENT.register(commandPerformEvent -> {
            ServerPlayer player = commandPerformEvent.getResults().getContext().getSource().getPlayer();
            if (player != null) {
                Services.COMMAND.insertAsync(
                        player.getUUID(),
                        player.getLevel(),
                        player.getOnPos(),
                        commandPerformEvent.getResults().getReader().getString()
                );
            }
            return EventResult.pass();
        });
    }
}
