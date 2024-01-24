package com.daqem.grieflogger.command.filter;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.model.action.Actions;
import com.daqem.grieflogger.model.action.IAction;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActionFilter implements IFilter {

    private final List<IAction> actions;

    public ActionFilter() {
        this(new ArrayList<>());
    }

    public ActionFilter(List<IAction> actions) {
        this.actions = actions;
    }

    @Override
    public String getName() {
        return GriefLogger.translate("filter.action").getString();
    }

    @Override
    public List<String> getOptions() {
        return Arrays.asList(Actions.ACTIONS.stream()
                .map(IAction::name)
                .map(String::toLowerCase)
                .toArray(String[]::new));
    }

    @Override
    public IFilter parse(StringReader reader, String suffix) throws CommandSyntaxException {
        String[] split = suffix.split(",");
        List<IAction> actions = new ArrayList<>(Actions.getActions(split));
        if (split.length != actions.size()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }
        return new ActionFilter(actions);
    }

    @Override
    public String toString() {
        return "ActionFilter{" +
                "actions=" + actions +
                '}';
    }

    public List<IAction> getActions() {
        return actions;
    }
}
