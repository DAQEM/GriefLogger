package com.daqem.grieflogger.command.filter;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.model.TimeUnit;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.List;

public class TimeFilter implements IFilter {

    private static final List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
    private final long time;

    public TimeFilter() {
        this(0, TimeUnit.MINUTE);
    }

    public TimeFilter(long time, TimeUnit timeUnit) {
        this.time = System.currentTimeMillis() - timeUnit.getMilliseconds() * time;
    }

    @Override
    public String getName() {
        return GriefLogger.translate("filter.time").getString();
    }

    @Override
    public List<String> getOptions() {
        return new ArrayList<>();
    }

    @Override
    public String[] listSuggestions(SuggestionsBuilder builder, String prefix, String suffix) {
        if (suffix.isEmpty()) {
            return numbers.stream().map(s -> getName() + ":" + s).toArray(String[]::new);
        } else {
            // Check if suffix ends with a time unit
            if (TimeUnit.getAbbreviations().stream().anyMatch(suffix::endsWith)) {
                //check if the suffix without the time unit is a number using Integer.parseInt
                String number = suffix.substring(0, suffix.length() - 1);
                try {
                    Integer.parseInt(number);
                    return new String[]{getName() + ":" + number + suffix.substring(suffix.length() - 1)};
                } catch (NumberFormatException e) {
                    return new String[]{};
                }
            }

            // Check if suffix is a number using Integer.parseInt
            try {
                Integer.parseInt(suffix);
                //add all time units to the suggestion
                return TimeUnit.getAbbreviations().stream().map(s -> getName() + ":" + suffix + s).toArray(String[]::new);
            } catch (NumberFormatException e) {
                return new String[]{};
            }
        }
    }

    @Override
    public IFilter parse(StringReader reader, String suffix) {
        TimeUnit timeUnit = TimeUnit.values()[TimeUnit.getAbbreviations().indexOf(suffix.substring(suffix.length() - 1))];
        int time = Integer.parseInt(suffix.substring(0, suffix.length() - timeUnit.getComponent().getString().length()));
        return new TimeFilter(time, timeUnit);
    }

    @Override
    public String toString() {
        return "TimeFilter{" +
                "time=" + time +
                '}';
    }

    public long getTime() {
        return time;
    }
}
