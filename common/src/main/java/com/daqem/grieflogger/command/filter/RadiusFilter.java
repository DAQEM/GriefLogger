package com.daqem.grieflogger.command.filter;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.model.BlockPosition;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RadiusFilter implements IFilter {

    private int radius;
    private BlockPosition position = new BlockPosition(0, 0, 0);

    public RadiusFilter() {
        this(0);
    }

    public RadiusFilter(int radius) {
        this.radius = radius;
        this.position = position;
    }

    @Override
    public String getName() {
        return GriefLogger.translate("filter.radius").getString();
    }

    @Override
public List<String> getOptions() {
    return IntStream.rangeClosed(1, 100)
                    .mapToObj(Integer::toString)
                    .collect(Collectors.toList());
}

    @Override
    public IFilter parse(StringReader reader, String suffix) {
        return new RadiusFilter(Integer.parseInt(suffix));
    }

    @Override
    public String toString() {
        return "RadiusFilter{" +
                "radius=" + radius +
                '}';
    }

    public int getMinX() {
        return position.x() - radius;
    }

    public int getMaxX() {
        return position.x() + radius;
    }

    public int getMinY() {
        return position.y() - radius;
    }

    public int getMaxY() {
        return position.y() + radius;
    }

    public int getMinZ() {
        return position.z() - radius;
    }

    public int getMaxZ() {
        return position.z() + radius;
    }

    public void setPosition(BlockPosition position) {
        this.position = position;
    }
}
