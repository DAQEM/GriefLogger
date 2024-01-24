package com.daqem.grieflogger.command;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.command.page.Page;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class PageCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("page")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("page", IntegerArgumentType.integer())
                        .executes(context -> page(context.getSource(), IntegerArgumentType.getInteger(context, "page"))));
    }

    private static int page(CommandSourceStack source, int page) {
        if (source.getPlayer() instanceof GriefLoggerServerPlayer player) {
            List<Page> lookupResults = player.grieflogger$getPages();
            if (page > 0 && page <= lookupResults.size()) {
                Page pageToDisplay = lookupResults.get(page - 1);
                pageToDisplay.sendToPlayer((ServerPlayer) player);
            } else {
                source.sendFailure(GriefLogger.translate("lookup.invalid_page", GriefLogger.getName()));
            }
        }
        return 1;
    }
}
