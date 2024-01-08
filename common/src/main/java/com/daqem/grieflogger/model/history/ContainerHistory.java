package com.daqem.grieflogger.model.history;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.model.BlockPosition;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.Time;
import com.daqem.grieflogger.model.User;
import com.daqem.grieflogger.model.action.ItemAction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ContainerHistory(Time time, User user, BlockPosition position, SimpleItemStack itemStack, ItemAction action) {

    public ContainerHistory(long time, String name, String uuid, int x, int y, int z, String material, byte[] data, int amount, int itemAction) {
        this(new Time(time), new User(name, UUID.fromString(uuid)), new BlockPosition(x, y, z), new SimpleItemStack(new ResourceLocation(material), amount, data), ItemAction.fromId(itemAction));
    }

    public Component getMaterialComponent() {
        MutableComponent mutableComponent = GriefLogger.themedLiteral(this.itemStack.getItem().arch$registryName().toString().replace("minecraft:", ""));
        return mutableComponent
                .withStyle(mutableComponent
                        .getStyle()
                        .withHoverEvent(
                                new HoverEvent(
                                        HoverEvent.Action.SHOW_ITEM,
                                        new HoverEvent.ItemStackInfo(itemStack.toItemStack()))));

    }
}
