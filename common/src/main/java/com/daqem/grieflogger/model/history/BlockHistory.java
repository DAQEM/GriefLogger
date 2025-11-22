package com.daqem.grieflogger.model.history;

import java.util.UUID;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.i18n.LanguageManager;
import com.daqem.grieflogger.model.BlockPosition;
import com.daqem.grieflogger.model.Time;
import com.daqem.grieflogger.model.User;
import com.daqem.grieflogger.model.action.BlockAction;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class BlockHistory extends History {


    private final String material;

    public BlockHistory(long time, String name, String uuid, int x, int y, int z, String material, int blockAction) {
        this(new Time(time), new User(name, UUID.fromString(uuid)), new BlockPosition(x, y, z), material, BlockAction.fromId(blockAction));
    }

    public BlockHistory(Time time, User user, BlockPosition position, String material, BlockAction action) {
        super(time, user, position, action);
        this.material = material;
    }

    @Override
    public Component getComponent() {
        return getTime().getFormattedTimeAgo().append(" ")
                .append(getAction().getPrefix()).append(" ")
                .append(getUser().getNameComponent()).append(" ")
                .append(getAction().getPastTense()).append(" ")
                .append(getMaterialComponent());
    }

    public Component getMaterialComponent() {
        Holder.Reference<Block> blockReference = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(material)).orElse(null);
        Item item = blockReference != null ? blockReference.value().asItem() : Items.AIR;
        MutableComponent mutableComponent;
        if (blockReference != null) {
            mutableComponent = GriefLogger.themedLiteral(LanguageManager.getString(blockReference.value().getDescriptionId()));
        } else {
            mutableComponent = GriefLogger.themedLiteral(this.material.replace("minecraft:", ""));
        }
        if (item != Items.AIR) {
            return mutableComponent
                    .withStyle(mutableComponent
                            .getStyle()
                            .withHoverEvent(
                                    new HoverEvent.ShowItem(
                                            item.getDefaultInstance()
                                    )));
        } else {
            return mutableComponent
                    .withStyle(mutableComponent
                            .getStyle()
                            .withHoverEvent(new HoverEvent.ShowText(
                                    Component.literal(this.material)
                            )));
        }
    }
}
