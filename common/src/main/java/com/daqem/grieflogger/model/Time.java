package com.daqem.grieflogger.model;

import com.daqem.grieflogger.GriefLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public record Time(long time) {

    private static final Component MINUTES = GriefLogger.translate("time.minutes");
    private static final Component HOURS = GriefLogger.translate("time.hours");
    private static final Component DAYS = GriefLogger.translate("time.days");
    private static final Component WEEKS = GriefLogger.translate("time.weeks");
    private static final Component MONTHS = GriefLogger.translate("time.months");
    private static final Component YEARS = GriefLogger.translate("time.years");

    private static final long MINUTE = 60 * 1000L;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final long WEEK = 7 * DAY;
    private static final long MONTH = 30 * DAY;
    private static final long YEAR = 365 * DAY;

    public Component getFormattedTimeAgo() {
        long timeAgo = System.currentTimeMillis() - time;
        if (timeAgo < HOUR) {
            return getTimeAgoComponent((double) timeAgo / MINUTE, MINUTES);
        } else if (timeAgo < DAY) {
            return getTimeAgoComponent((double) timeAgo / HOUR, HOURS);
        } else if (timeAgo < WEEK) {
            return getTimeAgoComponent((double) timeAgo / DAY, DAYS);
        } else if (timeAgo < MONTH) {
            return getTimeAgoComponent((double) timeAgo / WEEK, WEEKS);
        } else if (timeAgo < YEAR) {
            return getTimeAgoComponent((double) timeAgo / MONTH, MONTHS);
        } else {
            return getTimeAgoComponent((double) timeAgo / YEAR, YEARS);
        }
    }

    private static Component getTimeAgoComponent(double timeAgo, Component unit) {
        return GriefLogger.translate("inspect.time.ago", String.format("%.2f", timeAgo), unit)
                .withStyle(ChatFormatting.GRAY);
    }
}
