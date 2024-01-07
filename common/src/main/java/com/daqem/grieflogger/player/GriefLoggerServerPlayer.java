package com.daqem.grieflogger.player;

import com.daqem.grieflogger.model.BlockHistory;

import java.util.List;

public interface GriefLoggerServerPlayer extends GriefLoggerPlayer {

    boolean grieflogger$isInspecting();

    void grieflogger$setInspecting(boolean inspecting);

    void grieflogger$sendInspectMessage(List<BlockHistory> blockHistory);
}
