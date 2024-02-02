package com.daqem.grieflogger.command.filter;

import java.util.List;

public interface Filters {

    IFilter ACTION = new ActionFilter();
    IFilter EXCLUDE = new ExcludeFilter();
    IFilter INCLUDE = new IncludeFilter();
    IFilter RADIUS = new RadiusFilter();
    IFilter TIME = new TimeFilter();
    IFilter USER = new UserFilter();

    List<IFilter> FILTERS = List.of(ACTION, EXCLUDE, INCLUDE, RADIUS, TIME, USER);

    static IFilter fromPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return null;
        }
        if (prefix.length() == 1) {
            return FILTERS.stream()
                    .filter(x -> x.getPrefix() == prefix.charAt(0))
                    .findFirst()
                    .orElse(null);
        }
        return FILTERS.stream()
                .filter(x -> x.getName().toLowerCase().startsWith(prefix))
                .findFirst()
                .orElse(null);
    }

    static String[] getFilteredSuggestions(List<IFilter> filters, boolean hasItemFilter) {
        return FILTERS.stream()
                .filter(x -> !hasItemFilter || !(x instanceof ItemFilter))
                .filter(x -> filters.stream().noneMatch(y -> y.getClass() == x.getClass()))
                .map(x -> x.getName() + '.')
                .toArray(String[]::new);
    }
}
