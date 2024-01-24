package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.BlockService;
import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LogBlockEvent extends AbstractEvent {

    public static void logBlock(GriefLoggerServerPlayer player, Level level, BlockState state, BlockPos pos, BlockAction blockAction) {
        ResourceLocation materialLocation = state.getBlock().arch$registryName();
        if (materialLocation != null) {
            Services.BLOCK.insertMaterialAsync(
                    player.grieflogger$asServerPlayer().getUUID(),
                    level.dimension().location().toString(),
                    pos,
                    materialLocation.toString(),
                    blockAction);
        }
    }
}
