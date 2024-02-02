package com.daqem.grieflogger.command.argument;

import com.daqem.grieflogger.command.filter.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FilterArgument implements ArgumentType<IFilter> {

    public static FilterArgument filter() {
        return new FilterArgument();
    }

    @Override
    public IFilter parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        while (reader.canRead() && isAllowedInFilter(reader.peek())) {
            reader.skip();
        }

        int indexOfColon = reader.getString().indexOf('.', cursor);
        if (indexOfColon == -1) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, "Expected filter");
        }

        String prefix = reader.getString().substring(cursor, indexOfColon);
        IFilter filter = Filters.fromPrefix(prefix);
        if (filter == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, "Invalid filter");
        }

        String suffix = reader.getString().substring(indexOfColon + 1, reader.getCursor());
        return filter.parse(reader, suffix);
    }

    public boolean isAllowedInFilter(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-' || c == ',';
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(getSuggestions(context, builder), builder);
    }

    private <S> String[] getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String prefix = builder.getRemaining().toLowerCase().split("\\.")[0];
        String[] empty = new String[0];
        List<IFilter> filters = getFilters(context);

        boolean hasItemFilter = filters.stream().anyMatch(x -> x instanceof ItemFilter);

        if (prefix.isEmpty()) {
            return Filters.getFilteredSuggestions(filters, hasItemFilter);
        }

        IFilter filter = Filters.fromPrefix(prefix);
        if (filter == null
            || filter instanceof ItemFilter && hasItemFilter
            || filters.stream().anyMatch(x -> x != null && x.getClass().equals(filter.getClass()))) {
            return empty;
        }

        return filter.listSuggestions(builder);
    }

    public static IFilter getFilter(CommandContext<?> context, String name) throws CommandSyntaxException {
        var filter = context.getArgument(name, String.class);
        return new FilterArgument().parse(new StringReader(filter));
    }

    private <S> List<IFilter> getFilters(CommandContext<S> context) {
        return IntStream.rangeClosed(1, 5)
                .mapToObj(i -> getOptionalFilter(context, "filter" + i))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private <S> Optional<IFilter> getOptionalFilter(CommandContext<S> context, String key) {
        try {
            return Optional.of(context.getArgument(key, IFilter.class));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}