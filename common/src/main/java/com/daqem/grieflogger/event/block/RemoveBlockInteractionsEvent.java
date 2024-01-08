package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.BlockService;
import com.daqem.grieflogger.event.AbstractEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class RemoveBlockInteractionsEvent extends AbstractEvent {

    public static void removeBlockInteractions(Level level, BlockPos pos) {
        BlockService blockService = new BlockService(GriefLogger.getDatabase());
        blockService.removeInteractionsForPositionAsync(level, pos);
    }
}
