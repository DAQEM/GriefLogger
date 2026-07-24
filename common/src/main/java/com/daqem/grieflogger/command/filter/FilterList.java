package com.daqem.grieflogger.command.filter;

import com.daqem.grieflogger.model.BlockPosition;
import com.daqem.grieflogger.model.action.IAction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FilterList {
    private @Nullable ActionFilter actionFilter;
    private @Nullable ExcludeFilter excludeFilter;
    private @Nullable IncludeFilter includeFilter;
    private @Nullable RadiusFilter radiusFilter;
    private @Nullable TimeFilter timeFilter;
    private @Nullable UserFilter userFilter;

    public FilterList(List<IFilter> filters, CommandSourceStack source) {
        Map<Class<? extends IFilter>, IFilter> filterMap = filters.stream()
                .collect(Collectors.toMap(IFilter::getClass, Function.identity(), (a, b) -> b));

        actionFilter = (ActionFilter) filterMap.get(ActionFilter.class);
        excludeFilter = (ExcludeFilter) filterMap.get(ExcludeFilter.class);
        includeFilter = (IncludeFilter) filterMap.get(IncludeFilter.class);
        radiusFilter = (RadiusFilter) filterMap.get(RadiusFilter.class);
        timeFilter = (TimeFilter) filterMap.get(TimeFilter.class);
        userFilter = (UserFilter) filterMap.get(UserFilter.class);

        if (radiusFilter != null) {
            radiusFilter.setPosition(new BlockPosition((int) source.getPosition().x(), (int) source.getPosition().y(), (int) source.getPosition().z()));
        }
    }


    public Optional<ActionFilter> getActionFilter() {
        return Optional.ofNullable(actionFilter);
    }

    public @Nullable String getActionString() {
        return getActionFilter()
                .map(x -> x.getActions().stream()
                        .map(IAction::getId)
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .orElse(null);
    }

    public void setActionFilter(@Nullable ActionFilter actionFilter) {
        this.actionFilter = actionFilter;
    }

    public Optional<ExcludeFilter> getExcludeFilter() {
        return Optional.ofNullable(excludeFilter);
    }

    public @Nullable String getExcludeMaterialsString() {
        return getExcludeFilter()
                .map(x -> x.getMaterials().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("','")))
                .orElse(null);
    }

    public void setExcludeFilter(@Nullable ExcludeFilter excludeFilter) {
        this.excludeFilter = excludeFilter;
    }

    public Optional<IncludeFilter> getIncludeFilter() {
        return Optional.ofNullable(includeFilter);
    }

    public @Nullable String getIncludeMaterialsString() {
        return getIncludeFilter()
                .map(x -> x.getMaterials().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("','")))
                .orElse(null);
    }

    public void setIncludeFilter(@Nullable IncludeFilter includeFilter) {
        this.includeFilter = includeFilter;
    }

    public Optional<RadiusFilter> getRadiusFilter() {
        return Optional.ofNullable(radiusFilter);
    }

    public int getRadiusMinX() {
        return getRadiusFilter().map(RadiusFilter::getMinX).orElse(0);
    }

    public int getRadiusMaxX() {
        return getRadiusFilter().map(RadiusFilter::getMaxX).orElse(0);
    }

    public int getRadiusMinY() {
        return getRadiusFilter().map(RadiusFilter::getMinY).orElse(0);
    }

    public int getRadiusMaxY() {
        return getRadiusFilter().map(RadiusFilter::getMaxY).orElse(0);
    }

    public int getRadiusMinZ() {
        return getRadiusFilter().map(RadiusFilter::getMinZ).orElse(0);
    }

    public int getRadiusMaxZ() {
        return getRadiusFilter().map(RadiusFilter::getMaxZ).orElse(0);
    }

    public void setRadiusFilter(@Nullable RadiusFilter radiusFilter) {
        this.radiusFilter = radiusFilter;
    }

    public Optional<TimeFilter> getTimeFilter() {
        return Optional.ofNullable(timeFilter);
    }

    public long getTime() {
        return getTimeFilter().map(TimeFilter::getTime).orElse(0L);
    }

    public void setTimeFilter(@Nullable TimeFilter timeFilter) {
        this.timeFilter = timeFilter;
    }

    public Optional<UserFilter> getUserFilter() {
        return Optional.ofNullable(userFilter);
    }

    public @Nullable String getUserString() {
        return getUserFilter()
                .map(x -> x.getUserIds().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .orElse(null);
    }

    public void setUserFilter(@Nullable UserFilter userFilter) {
        this.userFilter = userFilter;
    }
}
