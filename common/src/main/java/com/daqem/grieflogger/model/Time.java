package com.daqem.grieflogger.model;

import com.daqem.grieflogger.GriefLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import java.util.Date;

public record Time(long time) {

    private static final Component MINUTES = GriefLogger.translate("time.minutes");
    private static final Component HOURS = GriefLogger.translate("time.hours");
    private static final Component DAYS = GriefLogger.translate("time.days");
    private static final Component YEARS = GriefLogger.translate("time.years");

    private static final long MINUTE = 60 * 1000L;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final long YEAR = 365 * DAY;

    public Component getFormattedTimeAgo() {
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

    private Component getTimeAgoComponent(double timeAgo, Component unit) {
        return GriefLogger.translate("inspect.time.ago", String.format("%.2f", timeAgo), unit)
                .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.GRAY)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                GriefLogger.literal(new Date(time).toString())
                                .withStyle(ChatFormatting.GRAY))));
    }
}
