package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.block.BlockHandler;
import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.daqem.knot.events.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class InspectDoorEvent {

    public static EventResult inspectDoor(GriefLoggerServerPlayer player, Level level, BlockPos pos, BlockState state, boolean isInteraction) {
        List<BlockPos> positions = new ArrayList<>(List.of(pos));
        BlockHandler.getSecondDoorPosition(pos, state).ifPresent(positions::add);
        if (isInteraction) {
            Services.BLOCK.getInteractionHistoryAsync(
                    level,
                    positions,
                    player::grieflogger$sendInspectMessage);
            return EventResult.INTERRUPT_FALSE;
        } else {
            Services.BLOCK.getBlockHistoryAsync(
                    level,
                    positions,
                    player::grieflogger$sendInspectMessage);
        }
        return EventResult.INTERRUPT_FALSE;
    }
}
