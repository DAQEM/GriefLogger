package com.daqem.grieflogger.block.container;

import net.minecraft.server.level.ServerPlayer;

public interface IContainerTransactionManager {
    void finalize(ServerPlayer serverPlayer);
}
