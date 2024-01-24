package com.daqem.grieflogger.command;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class InspectCommand implements ICommand {


    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("inspect")
                .requires(source -> source.hasPermission(2))
                .executes(context -> inspect(context.getSource()));
    }

    private static int inspect(CommandSourceStack source) {
        if (source.getPlayer() instanceof GriefLoggerServerPlayer player) {
            player.grieflogger$setInspecting(!player.grieflogger$isInspecting());
            source.sendSuccess(() -> GriefLogger.translate("commands.inspect." + (player.grieflogger$isInspecting() ? "enabled" : "disabled"), GriefLogger.getName()), false);
        }
        return 1;
    }
}
