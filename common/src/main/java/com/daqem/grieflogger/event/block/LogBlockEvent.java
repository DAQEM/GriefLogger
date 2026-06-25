package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LogBlockEvent extends AbstractEvent {

    public static void logBlock(GriefLoggerServerPlayer player, Level level, BlockState state, BlockPos pos, BlockAction blockAction) {
        ResourceLocation materialLocation = state.getBlock().arch$registryName();
        if (materialLocation != null) {
            ServerPlayer serverPlayer = player.grieflogger$asServerPlayer();
            // Ensure the actor's users row exists before logging their action, so the event's
            // user FK never resolves NULL (which would drop/abort the insert). (GAP E)
            Services.USER.ensure(serverPlayer.getUUID(), serverPlayer.getGameProfile().getName());
            Services.BLOCK.insertMaterial(
                    serverPlayer.getUUID(),
                    level.dimension().location().toString(),
                    pos,
                    materialLocation.toString(),
                    blockAction);
        }
    }
}
