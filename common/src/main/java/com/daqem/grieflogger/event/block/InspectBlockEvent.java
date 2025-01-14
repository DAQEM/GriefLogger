package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;

public class InspectBlockEvent extends AbstractEvent {

    public static InteractionResult inspectBlock(GriefLoggerServerPlayer player, BlockPos pos) {
        Services.BLOCK.getBlockHistoryAsync(
                player.grieflogger$asServerPlayer().level(),
                pos,
                player::grieflogger$sendInspectMessage);
        return InteractionResult.FAIL;
    }
}
