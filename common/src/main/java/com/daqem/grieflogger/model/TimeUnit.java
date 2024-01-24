package com.daqem.grieflogger.model;

import com.daqem.grieflogger.GriefLogger;
import net.minecraft.network.chat.Component;

import java.util.List;

public enum TimeUnit {
    MINUTE(60 * 1000L, GriefLogger.translate("time.minutes")),
    HOUR(60 * MINUTE.getMilliseconds(), GriefLogger.translate("time.hours")),
    DAY(24 * HOUR.getMilliseconds(), GriefLogger.translate("time.days")),
    YEAR(365 * DAY.getMilliseconds(), GriefLogger.translate("time.years"));

    private final long milliseconds;
    private final Component component;

    TimeUnit(long milliseconds, Component component) {
        this.milliseconds = milliseconds;
        this.component = component;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public Component getComponent() {
        return component;
    }

    public static List<String> getAbbreviations() {
        return List.of(
                MINUTE.component.getString(),
                HOUR.component.getString(),
                DAY.component.getString(),
                YEAR.component.getString()
        );
    }
}
