package com.daqem.grieflogger.command.filter;

import com.daqem.grieflogger.GriefLogger;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class ExcludeFilter extends ItemFilter {

    public ExcludeFilter() {
        super();
    }

    public ExcludeFilter(List<Item> items) {
        super(items);
    }

    @Override
    public String getName() {
        return GriefLogger.translate("filter.exclude").getString();
    }

    @Override
    public IFilter parse(StringReader reader, String suffix) throws CommandSyntaxException {
        return new ExcludeFilter(getItemsFromSuffix(reader, suffix));
    }
}
