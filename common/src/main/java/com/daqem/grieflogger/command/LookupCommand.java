package com.daqem.grieflogger.command;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.command.argument.FilterArgument;
import com.daqem.grieflogger.command.filter.*;
import com.daqem.grieflogger.command.page.Page;
import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.history.*;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LookupCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("lookup")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("filter1", FilterArgument.filter())
                        .then(Commands.argument("filter2", FilterArgument.filter())
                                .then(Commands.argument("filter3", FilterArgument.filter())
                                        .then(Commands.argument("filter4", FilterArgument.filter())
                                                .then(Commands.argument("filter5", FilterArgument.filter())
                                                        .executes(context -> lookup(context.getSource(), new FilterList(List.of(FilterArgument.getFilter(context, "filter1"), FilterArgument.getFilter(context, "filter2"), FilterArgument.getFilter(context, "filter3"), FilterArgument.getFilter(context, "filter4"), FilterArgument.getFilter(context, "filter5")), context.getSource()))))
                                                .executes(context -> lookup(context.getSource(), new FilterList(List.of(FilterArgument.getFilter(context, "filter1"), FilterArgument.getFilter(context, "filter2"), FilterArgument.getFilter(context, "filter3"), FilterArgument.getFilter(context, "filter4")), context.getSource()))))
                                        .executes(context -> lookup(context.getSource(), new FilterList(List.of(FilterArgument.getFilter(context, "filter1"), FilterArgument.getFilter(context, "filter2"), FilterArgument.getFilter(context, "filter3")), context.getSource()))))
                                .executes(context -> lookup(context.getSource(), new FilterList(List.of(FilterArgument.getFilter(context, "filter1"), FilterArgument.getFilter(context, "filter2")), context.getSource()))))
                .executes(context -> lookup(context.getSource(), new FilterList(List.of(FilterArgument.getFilter(context, "filter1")), context.getSource()))));
    }

    @SuppressWarnings("SameReturnValue")
    private static int lookup(CommandSourceStack source, FilterList filterList) {
        if (source.getPlayer() instanceof GriefLoggerServerPlayer player) {
            List<IHistory> filteredHistory = getHistory(source.getLevel(), filterList);
            if (filteredHistory.isEmpty()) {
                source.sendFailure(GriefLogger.translate("lookup.no_results", GriefLogger.getName()));
                return 1;
            }
            List<Page> pages = Page.convertToPages(filteredHistory, false);
            player.grieflogger$setPages(pages);
            Page pageToDisplay = pages.get(0);
            pageToDisplay.sendToPlayer((ServerPlayer) player);
        }
        return 1;
    }

    private static List<IHistory> getHistory(Level level, FilterList filterList) {
        List<SessionHistory> filteredSessionHistory = Services.SESSION.getFilteredSessionHistory(level, filterList);
        List<IHistory> filteredBlockHistory = Services.BLOCK.getFilteredBlockHistory(level, filterList);
        List<IHistory> filteredContainerHistory = Services.CONTAINER.getFilteredContainerHistory(level, filterList);
        List<ItemHistory> filteredItemHistory = Services.ITEM.getFilteredItemHistory(level, filterList);
        return new ArrayList<>(List.of(filteredSessionHistory, filteredBlockHistory, filteredContainerHistory, filteredItemHistory))
                .stream()
                .flatMap(List::stream)
                .sorted((x, y) -> Long.compare(y.getTime().time(), x.getTime().time()))
                .collect(Collectors.toList());
    }
}
