package com.daqem.grieflogger.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class GriefLoggerCommand {

    private static final ICommand INSPECT = new InspectCommand();
    private static final ICommand LOOKUP = new LookupCommand();
    private static final ICommand PAGE = new PageCommand();

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(commandWithPrefix("grieflogger"));
        dispatcher.register(commandWithPrefix("gl"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> commandWithPrefix(String prefix) {
        return Commands.literal(prefix)
                .then(INSPECT.getCommand())
                .then(LOOKUP.getCommand())
                .then(PAGE.getCommand());
    }
}
