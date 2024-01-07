package com.daqem.grieflogger.model;

import com.daqem.grieflogger.GriefLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public record BlockHistory(Time time, User user, String level, BlockPosition position, String material, BlockAction blockAction) {
    public Component getMaterialComponent() {
        MutableComponent mutableComponent = GriefLogger.themedLiteral(this.material.replace("minecraft:", ""));
        return mutableComponent
                .withStyle(mutableComponent
                        .getStyle()
                        .withHoverEvent(
                                new HoverEvent(
                                        HoverEvent.Action.SHOW_ITEM,
                                        new HoverEvent.ItemStackInfo(
                                                BuiltInRegistries.BLOCK.get(
                                                        new ResourceLocation(material)
                                                ).asItem()
                                                        .getDefaultInstance()))));

    }
}
