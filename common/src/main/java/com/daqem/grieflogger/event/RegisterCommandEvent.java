package com.daqem.grieflogger.event;

import com.daqem.grieflogger.command.GriefLoggerCommand;
import com.daqem.knot.events.EventsService;

public class RegisterCommandEvent {

    public static void registerEvent() {
        EventsService.Server.COMMAND_REGISTER.register((dispatcher, registry, selection) ->
                GriefLoggerCommand.registerCommand(dispatcher));
    }
}
