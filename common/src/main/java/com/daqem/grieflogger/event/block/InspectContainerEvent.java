package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.ContainerService;
import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

public class InspectContainerEvent extends AbstractEvent {

    public static EventResult inspectContainer(GriefLoggerServerPlayer serverPlayer, Level level, BlockPos pos) {
        ContainerService containerService = new ContainerService(GriefLogger.getDatabase());
        containerService.getHistoryAsync(
                level,
                pos,
                serverPlayer::grieflogger$sendContainerInspectMessage);
        return interrupt();
    }
}
