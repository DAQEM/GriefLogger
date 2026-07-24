package com.daqem.grieflogger.event;

import com.daqem.grieflogger.command.GriefLoggerCommand;
import dev.architectury.event.events.common.CommandRegistrationEvent;

public class RegisterCommandEvent {

    public static void registerEvent() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) ->
                GriefLoggerCommand.registerCommand(dispatcher));
    }
}
