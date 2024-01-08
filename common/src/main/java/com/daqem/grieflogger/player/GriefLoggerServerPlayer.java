package com.daqem.grieflogger.player;

import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.model.history.BlockHistory;
import com.daqem.grieflogger.model.history.ContainerHistory;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface GriefLoggerServerPlayer extends GriefLoggerPlayer {

    boolean grieflogger$isInspecting();
    void grieflogger$setInspecting(boolean inspecting);
    void grieflogger$sendBlockInspectMessage(List<BlockHistory> blockHistory);
    void grieflogger$sendContainerInspectMessage(List<ContainerHistory> containerHistory);
    void griefLogger$addItemToQueue(ItemAction action, SimpleItemStack itemStack);
    ServerPlayer grieflogger$asServerPlayer();
}
