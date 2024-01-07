package com.daqem.grieflogger.model;

import com.daqem.grieflogger.GriefLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public record BlockPosition(int x, int y, int z) {

    public Component getComponent() {
        return GriefLogger.translate("inspect.position", x, y, z)
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, GriefLogger.literal("Click to teleport to this position.")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + x + " " + y + " " + z)));
    }
}
