package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.block.BlockHandler;
import com.daqem.grieflogger.database.service.BlockService;
import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.event.AbstractEvent;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RemoveDoorInteractionsEvent extends AbstractEvent {

    public static void removeDoorInteractions(Level level, BlockPos pos, BlockState state) {
        List<BlockPos> positions = new ArrayList<>(List.of(pos));
        BlockHandler.getSecondDoorPosition(pos, state).ifPresent(positions::add);
        for (BlockPos position : positions) {
            Services.BLOCK.removeInteractionsForPositionAsync(level, position);
        }
    }
}
