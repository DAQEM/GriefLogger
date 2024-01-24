package com.daqem.grieflogger.command.filter;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.cache.Caches;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserFilter implements IFilter {

    private final Map<Integer, String> usernames;

    public UserFilter() {
        this(new HashMap<>());
    }

    public UserFilter(Map<Integer, String> usernames) {
        this.usernames = usernames;
    }

    @Override
    public String getName() {
        return GriefLogger.translate("filter.user").getString();
    }

    @Override
    public List<String> getOptions() {
        return Caches.USER.getAllUsernames().values().stream().toList();
    }

    @Override
    public IFilter parse(StringReader reader, String suffix) throws CommandSyntaxException {
        String[] split = suffix.split(",");
        Map<Integer, String> usernames = Caches.USER.getAllUsernames().entrySet().stream()
                .filter(entry -> Arrays.asList(split).contains(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (split.length != usernames.size()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }
        return new UserFilter(usernames);
    }

    @Override
    public String toString() {
        return "UserFilter{" +
                "usernames=" + usernames +
                '}';
    }

    public List<Integer> getUserIds() {
        return usernames.keySet().stream().toList();
    }
}
