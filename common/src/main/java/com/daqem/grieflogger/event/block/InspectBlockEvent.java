package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.BlockService;
import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;

public class InspectBlockEvent extends AbstractEvent {

    public static EventResult inspectBlock(GriefLoggerServerPlayer player, BlockPos pos) {
        BlockService blockService = new BlockService(GriefLogger.getDatabase());
        blockService.getHistoryAsync(
                player.grieflogger$asServerPlayer().level(),
                pos,
                player::grieflogger$sendBlockInspectMessage);
        return interrupt();
    }
}
