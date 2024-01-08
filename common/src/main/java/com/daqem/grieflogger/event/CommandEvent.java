package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.CommandService;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandPerformEvent;
import net.minecraft.server.level.ServerPlayer;

public class CommandEvent {

    public static void registerEvent() {
        CommandPerformEvent.EVENT.register(commandPerformEvent -> {
            CommandService commandService = new CommandService(GriefLogger.getDatabase());
            ServerPlayer player = commandPerformEvent.getResults().getContext().getSource().getPlayer();
            commandService.insertAsync(
                    player.getUUID(),
                    player.level(),
                    player.getOnPos(),
                    commandPerformEvent.getResults().getReader().getString()
            );
            return EventResult.pass();
        });
    }
}
