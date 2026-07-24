package com.daqem.grieflogger.model.action;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Actions {

    List<IAction> ACTIONS = Stream.of(
                    BlockAction.values(),
                    SessionAction.values(),
                    ItemAction.values()
            )
            .flatMap(Arrays::stream)
            .collect(Collectors.toList());

    static IAction getAction(String str) {
        return ACTIONS.stream()
                .filter(action -> action.name().equalsIgnoreCase(str))
                .findFirst()
                .orElse(null);
    }

    static List<IAction> getActions(String[] split) {
        return ACTIONS.stream()
                .filter(action -> Arrays.asList(split).contains(action.name().toLowerCase()))
                .collect(Collectors.toList());
    }
}