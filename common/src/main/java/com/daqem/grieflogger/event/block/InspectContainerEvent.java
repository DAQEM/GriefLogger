package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class InspectContainerEvent extends AbstractEvent {

    public static EventResult inspectContainer(GriefLoggerServerPlayer serverPlayer, Level level, BlockPos pos) {
        Services.CONTAINER.getHistoryAsync(
                level,
                pos,
                serverPlayer::grieflogger$sendInspectMessage);
        return interrupt();
    }
}
