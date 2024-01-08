package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.block.BlockHandler;
import com.daqem.grieflogger.database.service.BlockService;
import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class InspectDoorEvent extends AbstractEvent {

    public static EventResult inspectDoor(GriefLoggerServerPlayer player, Level level, BlockPos pos, BlockState state) {
        List<BlockPos> positions = new ArrayList<>(List.of(pos));
        BlockHandler.getSecondDoorPosition(pos, state).ifPresent(positions::add);
        BlockService blockService = new BlockService(GriefLogger.getDatabase());
        blockService.getHistoryAsync(
                level,
                positions,
                player::grieflogger$sendBlockInspectMessage);
        return interrupt();
    }
}
