package com.daqem.grieflogger.event.item;

import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CraftItemEvent extends AbstractEvent {

    public static void onCraftItem(Player player, ItemStack itemStack, Container container) {
        if (player instanceof GriefLoggerServerPlayer serverPlayer) {
            serverPlayer.griefLogger$addItemToQueue(ItemAction.CRAFT_ITEM, new SimpleItemStack(itemStack));
        }
    }
}
