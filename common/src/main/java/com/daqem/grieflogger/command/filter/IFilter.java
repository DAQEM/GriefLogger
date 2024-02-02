package com.daqem.grieflogger.command.filter;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Arrays;
import java.util.List;

public interface IFilter {

    default char getPrefix() {
        return getName().charAt(0);
    }

    String getName();

    List<String> getOptions();

    default String[] listSuggestions(SuggestionsBuilder builder, String prefix, String suffix) {
        if (suffix.contains(",")) {
            int lastIndexOf = suffix.lastIndexOf(",");
            String[] usedUsernames = suffix.substring(0, lastIndexOf).split(",");
            String suffixPrefix = suffix.substring(0, lastIndexOf);

            return getOptions().stream()
                    .filter(s -> !Arrays.asList(usedUsernames).contains(s))
                    .map(s -> getName() + '.' + suffixPrefix + "," + s)
                    .toArray(String[]::new);
        }

        return getOptions().stream()
                .map(s -> getName() + '.' + s)
                .toArray(String[]::new);
    }

    IFilter parse(StringReader reader, String suffix) throws CommandSyntaxException;

    default String[] listSuggestions(SuggestionsBuilder builder) {
        String str = builder.getRemaining();
        String[] split = str.split("\\.");
        String prefix = split[0];
        String suffix = split.length > 1 ? split[1] : "";

        return listSuggestions(builder, prefix, suffix);
    }
}
