package com.daqem.grieflogger.command;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.GriefLoggerPermissions;
import com.daqem.grieflogger.command.argument.FilterArgument;
import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.command.filter.IFilter;
import com.daqem.grieflogger.command.page.Page;
import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.history.IHistory;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.daqem.grieflogger.thread.ThreadManager;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LookupCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("lookup")
                .requires(source -> GriefLoggerPermissions.check(source, "grieflogger.command.lookup", 2))
                .then(Commands.argument("filters", StringArgumentType.greedyString())
                        .suggests(LookupCommand::suggestFilters)
                        .executes(context -> lookup(context.getSource(), StringArgumentType.getString(context, "filters"))))
                .executes(context -> lookup(context.getSource(), ""));
    }

    // ... rest of the methods (lookup, suggestFilters, getHistory) remain exactly the same as original ...
    private static int lookup(CommandSourceStack source, String filtersInput) {
        List<IFilter> filters = new ArrayList<>();

        if (!filtersInput.isBlank()) {
            String[] parts = filtersInput.split("\\s+");
            FilterArgument parser = new FilterArgument();

            for (String part : parts) {
                try {
                    IFilter filter = parser.parse(new StringReader(part));
                    filters.add(filter);
                } catch (CommandSyntaxException e) {
                    source.sendFailure(GriefLogger.translate("lookup.invalid_filter", GriefLogger.getName(), part));
                    return 0;
                }
            }
        }

        return lookup(source, new FilterList(filters, source));
    }

    private static int lookup(CommandSourceStack source, FilterList filterList) {
        if (source.getPlayer() instanceof GriefLoggerServerPlayer player) {
            ThreadManager.submit(() -> getHistory(source.getLevel(), filterList), filteredHistory -> {
                if (filteredHistory.isEmpty()) {
                    source.sendFailure(GriefLogger.translate("lookup.no_results", GriefLogger.getName()));
                    return;
                }
                List<Page> pages = Page.convertToPages(filteredHistory, false);
                player.grieflogger$setPages(pages);
                Page pageToDisplay = pages.getFirst();
                pageToDisplay.sendToPlayer((ServerPlayer) player);
            });
        }
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestFilters(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        int lastSpace = remaining.lastIndexOf(' ');
        int start = builder.getStart() + lastSpace + 1;
        SuggestionsBuilder offsetBuilder = builder.createOffset(start);
        return new FilterArgument().listSuggestions(context, offsetBuilder);
    }

    private static List<IHistory> getHistory(Level level, FilterList filterList) {
        List<IHistory> history = new ArrayList<>();
        history.addAll(Services.BLOCK.getFilteredBlockHistory(level, filterList));
        history.addAll(Services.SESSION.getFilteredSessionHistory(level, filterList));
        history.addAll(Services.CONTAINER.getFilteredContainerHistory(level, filterList));
        history.addAll(Services.ITEM.getFilteredItemHistory(level, filterList));
        return history.stream()
                .sorted((x, y) -> Long.compare(y.getTime().time(), x.getTime().time()))
                .collect(Collectors.toList());
    }
}