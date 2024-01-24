package com.daqem.grieflogger.event.item;

import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import dev.architectury.event.EventResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public class DropItemEvent extends AbstractEvent {

    public static EventResult onDropItem(Player player, ItemEntity itemEntity) {
        if (player instanceof GriefLoggerServerPlayer serverPlayer) {
            serverPlayer.griefLogger$addItemToQueue(ItemAction.DROP_ITEM, new SimpleItemStack(itemEntity.getItem()));
        }
        return pass();
    }
}
