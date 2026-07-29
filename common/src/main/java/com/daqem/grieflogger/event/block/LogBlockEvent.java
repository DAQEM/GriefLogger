package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LogBlockEvent {

    public static void logBlock(GriefLoggerServerPlayer player, Level level, BlockState state, BlockPos pos, BlockAction blockAction) {
        Identifier materialLocation = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        Services.BLOCK.insertMaterial(
                player.grieflogger$asServerPlayer().getUUID(),
                level.dimension().identifier().toString(),
                pos,
                materialLocation.toString(),
                blockAction);
    }
}
