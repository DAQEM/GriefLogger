package com.daqem.grieflogger.model;

import com.daqem.grieflogger.GriefLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public record BlockPosition(int x, int y, int z) {

    public Component getComponent() {
        return GriefLogger.translate("lookup.position", x, y, z)
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withHoverEvent(new HoverEvent.ShowText(GriefLogger.literal("Click to teleport to this position.")))
                        .withClickEvent(new ClickEvent.RunCommand("/tp " + x + " " + y + " " + z)));
    }
}
