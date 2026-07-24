package com.daqem.grieflogger.player;

import com.daqem.grieflogger.command.page.Page;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.model.history.BlockHistory;
import com.daqem.grieflogger.model.history.ContainerHistory;
import com.daqem.grieflogger.model.history.IHistory;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface GriefLoggerServerPlayer extends GriefLoggerPlayer {

    boolean grieflogger$isInspecting();
    void grieflogger$setInspecting(boolean inspecting);
    void grieflogger$sendInspectMessage(List<IHistory> history);
    void griefLogger$addItemToQueue(ItemAction action, SimpleItemStack itemStack);
    ServerPlayer grieflogger$asServerPlayer();
    List<Page> grieflogger$getPages();
    void grieflogger$setPages(List<Page> pages);
}
