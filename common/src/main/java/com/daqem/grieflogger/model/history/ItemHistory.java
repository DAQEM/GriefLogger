package com.daqem.grieflogger.model.history;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.model.BlockPosition;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.Time;
import com.daqem.grieflogger.model.User;
import com.daqem.grieflogger.model.action.IAction;
import com.daqem.grieflogger.model.action.ItemAction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class ItemHistory extends History {

    protected final SimpleItemStack itemStack;

    public ItemHistory(long time, String name, String uuid, int x, int y, int z, String material, byte[] data, int amount, int action) {
        this(new Time(time), new User(name, UUID.fromString(uuid)), new BlockPosition(x, y, z), new SimpleItemStack(new ResourceLocation(material), amount, data), ItemAction.fromId(action));
    }

    public ItemHistory(Time time, User user, BlockPosition position, SimpleItemStack itemStack, IAction action) {
        super(time, user, position, action);
        this.itemStack = itemStack;
    }

    public SimpleItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public Component getComponent() {
        return GriefLogger.translate("lookup.container.history_entry",
                getTime().getFormattedTimeAgo(),
                getAction().getPrefix(),
                getUser().getNameComponent(),
                getAction().getPastTense(),
                getItemStack().getCount(),
                getMaterialComponent()
        );
    }

    @Override
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
