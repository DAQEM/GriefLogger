package com.daqem.grieflogger.command.filter;

import com.daqem.grieflogger.GriefLogger;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class IncludeFilter extends ItemFilter {

    public IncludeFilter() {
        super();
    }

    public IncludeFilter(List<Item> items) {
        super(items);
    }

    @Override
    public String getName() {
        return GriefLogger.translate("filter.include").getString();
    }

    @Override
    public IFilter parse(StringReader reader, String suffix) throws CommandSyntaxException {
        return new IncludeFilter(getItemsFromSuffix(reader, suffix));
    }
}
