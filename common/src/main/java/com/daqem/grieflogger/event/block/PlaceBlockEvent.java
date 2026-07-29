package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.util.EntityUtils;
import com.daqem.knot.events.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceBlockEvent {

    public static EventResult placeBlock(Level level, BlockPos pos, BlockState state, Entity placer) {
        EntityUtils.logBlockAction(level, pos, state, placer, placer, BlockAction.PLACE_BLOCK);
        return EventResult.PASS;
    }
}
