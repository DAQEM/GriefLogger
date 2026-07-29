package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.database.service.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class RemoveBlockInteractionsEvent {

    public static void removeBlockInteractions(Level level, BlockPos pos) {
        Services.BLOCK.removeInteractionsForPosition(level, pos);
    }
}
