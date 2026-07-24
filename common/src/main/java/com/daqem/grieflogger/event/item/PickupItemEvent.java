package com.daqem.grieflogger.event.item;

import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PickupItemEvent extends AbstractEvent {

    public static void onPickupItem(Player player, ItemEntity itemEntity, ItemStack itemStack) {
        if (player instanceof GriefLoggerServerPlayer serverPlayer) {
            serverPlayer.griefLogger$addItemToQueue(ItemAction.PICKUP_ITEM, new SimpleItemStack(itemStack));
        }
    }
}
