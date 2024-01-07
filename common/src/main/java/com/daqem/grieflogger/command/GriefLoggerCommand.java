package com.daqem.grieflogger.command;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class GriefLoggerCommand {

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(commandWithPrefix("grieflogger"));
        dispatcher.register(commandWithPrefix("grief"));
        dispatcher.register(commandWithPrefix("gl"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> commandWithPrefix(String prefix) {
        return Commands.literal(prefix)
                .then(inspectWithPrefix("inspect"))
                .then(inspectWithPrefix("i"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> inspectWithPrefix(String prefix) {
        return Commands.literal(prefix)
                .executes(context -> inspect(context.getSource()));
    }

    private static int inspect(CommandSourceStack source) {
        if (source.getPlayer() instanceof GriefLoggerServerPlayer player) {
            player.grieflogger$setInspecting(!player.grieflogger$isInspecting());
            source.sendSuccess(() -> GriefLogger.translate("commands.inspect." + (player.grieflogger$isInspecting() ? "enabled" : "disabled")), false);
        }
        return 1;
    }
}
