package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceBlockEvent {

    public static EventResult placeBlock(Level level, BlockPos pos, BlockState state, Entity placer) {
        if (placer instanceof GriefLoggerServerPlayer serverPlayer) {
            LogBlockEvent.logBlock(serverPlayer, level, state, pos, BlockAction.PLACE);
        }
        return EventResult.pass();
    }
}
