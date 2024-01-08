package com.daqem.grieflogger.event.item;

import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ConsumeItemEvent {

    public static void consumeItem(Player player, ItemStack itemStack) {
        if (player instanceof GriefLoggerServerPlayer serverPlayer) {
            serverPlayer.griefLogger$addItemToQueue(ItemAction.CONSUME, new SimpleItemStack(itemStack));
        }
    }
}
