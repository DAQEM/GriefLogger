package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.block.BlockHandler;
import com.daqem.grieflogger.block.container.ContainerHandler;
import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class RightClickBlockEvent extends AbstractEvent {

    public static EventResult rightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction direction) {
        if (player instanceof GriefLoggerServerPlayer serverPlayer) {
            if (hand == InteractionHand.MAIN_HAND) {

                Level level = player.level();
                BlockState state = level.getBlockState(pos);
                Block block = state.getBlock();

                if (serverPlayer.grieflogger$isInspecting()) {
                    if (state.getBlock() instanceof DoorBlock) {
                        return InspectDoorEvent.inspectDoor(serverPlayer, level, pos, state, true);
                    }
                    if (state.hasBlockEntity()) {
                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        Optional<BaseContainerBlockEntity> container = ContainerHandler.getContainer(blockEntity);
                        if (container.isPresent()) {
                            return InspectContainerEvent.inspectContainer(serverPlayer, level, pos);
                        }
                    }
                    return InspectBlockEvent.inspectBlock(serverPlayer, pos.relative(direction));
                }

                if (BlockHandler.isBlockIntractable(block)) {
                    LogBlockEvent.logBlock(serverPlayer, level, state, pos, BlockAction.INTERACT);
                }
            }
        }
        return pass();
    }
}
