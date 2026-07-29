package com.daqem.grieflogger.command;

import com.daqem.knot.Knot;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.PermissionLevel;

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
                .requires(source -> Knot.PERMISSIONS.check(source, "grieflogger.command", PermissionLevel.GAMEMASTERS))
                .then(INSPECT.getCommand())
                .then(LOOKUP.getCommand())
                .then(PAGE.getCommand());
    }
}