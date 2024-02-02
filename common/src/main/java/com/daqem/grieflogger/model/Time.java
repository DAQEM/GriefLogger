package com.daqem.grieflogger.model;

import com.daqem.grieflogger.GriefLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Date;

public record Time(long time) {

    private static final Component MINUTES = TimeUnit.MINUTE.getComponent();
    private static final Component HOURS = TimeUnit.HOUR.getComponent();
    private static final Component DAYS = TimeUnit.DAY.getComponent();
    private static final Component YEARS = TimeUnit.YEAR.getComponent();

    private static final long MINUTE = TimeUnit.MINUTE.getMilliseconds();
    private static final long HOUR = TimeUnit.HOUR.getMilliseconds();
    private static final long DAY = TimeUnit.DAY.getMilliseconds();
    private static final long YEAR = TimeUnit.YEAR.getMilliseconds();

    public MutableComponent getFormattedTimeAgo() {
        long timeAgo = System.currentTimeMillis() - time;
        if (timeAgo < HOUR / 2) {
            return getTimeAgoComponent((double) timeAgo / MINUTE, MINUTES);
        } else if (timeAgo < DAY / 2) {
            return getTimeAgoComponent((double) timeAgo / HOUR, HOURS);
        } else if (timeAgo < YEAR / 2) {
            return getTimeAgoComponent((double) timeAgo / DAY, DAYS);
        } else {
            return getTimeAgoComponent((double) timeAgo / YEAR, YEARS);
        }
    }

    private MutableComponent getTimeAgoComponent(double timeAgo, Component unit) {
        return GriefLogger.translate("lookup.time.ago", String.format("%.2f", timeAgo), GriefLogger.translate("time.divider"), unit)
                .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.GRAY)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                GriefLogger.literal(new Date(time).toString())
                                .withStyle(ChatFormatting.GRAY))));
    }
}
