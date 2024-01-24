package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.event.AbstractEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class RemoveBlockInteractionsEvent extends AbstractEvent {

    public static void removeBlockInteractions(Level level, BlockPos pos) {
        Services.BLOCK.removeInteractionsForPositionAsync(level, pos);
    }
}
