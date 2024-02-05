package com.daqem.grieflogger.command.filter;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class ItemFilter implements IFilter {

    private final List<Item> items;

    public ItemFilter() {
        this(new ArrayList<>());
    }

    public ItemFilter(List<Item> items) {
        this.items = items;
    }

    @Override
    public List<String> getOptions() {
        return new ArrayList<>(Registry.ITEM.stream().map(x -> x.arch$registryName().toString().replace("minecraft:", "")).toList());
    }

    protected List<Item> getItemsFromSuffix(StringReader reader, String suffix) throws CommandSyntaxException {
        String[] split = suffix.split(",");
        List<Item> items = Registry.ITEM.stream()
                .filter(item -> Arrays.asList(split).contains(item.arch$registryName().toString().replace("minecraft:", "")))
                .toList();
        if (split.length != items.size()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }
        return items;
    }

    @Override
    public String toString() {
        return "ItemFilter{" +
                "items=" + items.stream().map(Item::arch$registryName).toList() +
                '}';
    }

    public List<String> getMaterials() {
        return items.stream()
                .map(Item::arch$registryName)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(x -> x.replace("minecraft:", ""))
                .toList();
    }
}
