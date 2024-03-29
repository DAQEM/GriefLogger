package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.block.container.ContainerHandler;
import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

import java.util.List;

public class BreakContainerEvent {

    public static void breakContainer(GriefLoggerServerPlayer player, Level level, BlockPos pos, BaseContainerBlockEntity containerBlockEntity) {
        List<SimpleItemStack> itemStacks = ContainerHandler.getContainerItems(containerBlockEntity);
        Services.CONTAINER.insertListAsync(
                player.grieflogger$asServerPlayer().getUUID(),
                level,
                pos,
                itemStacks,
                ItemAction.REMOVE_ITEM);
    }
}
