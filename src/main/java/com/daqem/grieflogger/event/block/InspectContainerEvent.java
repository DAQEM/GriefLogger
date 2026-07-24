package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.model.history.IHistory;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.daqem.grieflogger.thread.ThreadManager;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class InspectContainerEvent extends AbstractEvent {

    public static EventResult inspectContainer(GriefLoggerServerPlayer serverPlayer, Level level, BlockPos pos) {
        ThreadManager.submit(() -> {
            List<IHistory> history = new ArrayList<>();
            List<IHistory> containerHistory = Services.CONTAINER.getHistory(
                    level,
                    pos);
            List<IHistory> interactionHistory = Services.BLOCK.getInteractionHistory(
                    level,
                    pos
            );
            history.addAll(containerHistory);
            history.addAll(interactionHistory);
            history.sort((a, b) -> Long.compare(b.getTime().time(), a.getTime().time()));
            return history;
        }, serverPlayer::grieflogger$sendInspectMessage);
        return interrupt();
    }

    public static EventResult inspectContainers(GriefLoggerServerPlayer serverPlayer, Level level, BlockPos pos, BlockPos connectionPos) {
        ThreadManager.submit(() -> {
            List<IHistory> history = new ArrayList<>();
            List<IHistory> containerHistory = Services.CONTAINER.getHistory(
                    level,
                    pos,
                    connectionPos);
            List<IHistory> interactionHistory = Services.BLOCK.getInteractionHistory(
                    level,
                    List.of(pos, connectionPos)
            );
            history.addAll(containerHistory);
            history.addAll(interactionHistory);
            history.sort((a, b) -> Long.compare(b.getTime().time(), a.getTime().time()));
            return history;
        }, serverPlayer::grieflogger$sendInspectMessage);
        return interrupt();
    }
}
