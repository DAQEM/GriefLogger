package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.daqem.knot.events.EventResult;
import net.minecraft.core.BlockPos;

public class InspectBlockEvent {

    public static EventResult inspectBlock(GriefLoggerServerPlayer player, BlockPos pos) {
        Services.BLOCK.getBlockHistoryAsync(
                player.grieflogger$asServerPlayer().level(),
                pos,
                player::grieflogger$sendInspectMessage);
        return EventResult.INTERRUPT_FALSE;
    }
}
