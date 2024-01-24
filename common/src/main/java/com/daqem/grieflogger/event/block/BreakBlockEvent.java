package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.block.BlockHandler;
import com.daqem.grieflogger.block.container.ContainerHandler;
import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import dev.architectury.event.EventResult;
import dev.architectury.utils.value.IntValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BreakBlockEvent extends AbstractEvent {

    public static EventResult breakBlock(Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
        if (player instanceof GriefLoggerServerPlayer serverPlayer) {
            if (serverPlayer.grieflogger$isInspecting()) {
                return interrupt();
            }

            Block block = state.getBlock();
            if (BlockHandler.isBlockIntractable(block)) {
                if (block instanceof DoorBlock) {
                    RemoveDoorInteractionsEvent.removeDoorInteractions(level, pos, state);
                } else {
                    RemoveBlockInteractionsEvent.removeBlockInteractions(level, pos);
                }
            }
            if (state.hasBlockEntity()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                ContainerHandler.getContainer(blockEntity).ifPresent(container ->
                        BreakContainerEvent.breakContainer(serverPlayer, level, pos, container));
            }

            LogBlockEvent.logBlock(serverPlayer, level, state, pos, BlockAction.BREAK_BLOCK);
        }
        return pass();
    }
}
