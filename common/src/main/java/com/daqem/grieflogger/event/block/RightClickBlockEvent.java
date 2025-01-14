package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.block.BlockHandler;
import com.daqem.grieflogger.block.container.ContainerHandler;
import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.Optional;

public class RightClickBlockEvent extends AbstractEvent {

    public static InteractionResult rightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction direction) {
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
                            if (state.hasProperty(ChestBlock.TYPE)) {
                                ChestType chestType = state.getValue(ChestBlock.TYPE);
                                if (chestType != ChestType.SINGLE) {
                                    Direction connectionDirection = state.getValue(ChestBlock.FACING);
                                    BlockPos connectionPos = pos;
                                    if (chestType == ChestType.LEFT) {
                                        if (connectionDirection == Direction.NORTH) {
                                            connectionPos = pos.east();
                                        } else if (connectionDirection == Direction.SOUTH) {
                                            connectionPos = pos.west();
                                        } else if (connectionDirection == Direction.WEST) {
                                            connectionPos = pos.north();
                                        } else {
                                            connectionPos = pos.south();
                                        }
                                    }
                                    if (chestType == ChestType.RIGHT) {
                                        if (connectionDirection == Direction.NORTH) {
                                            connectionPos = pos.west();
                                        } else if (connectionDirection == Direction.SOUTH) {
                                            connectionPos = pos.east();
                                        } else if (connectionDirection == Direction.WEST) {
                                            connectionPos = pos.south();
                                        } else {
                                            connectionPos = pos.north();
                                        }
                                    }
                                    return InspectContainerEvent.inspectContainers(serverPlayer, level, pos, connectionPos);
                                }
                            }
                            return InspectContainerEvent.inspectContainer(serverPlayer, level, pos);
                        }
                    }
                    return InspectBlockEvent.inspectBlock(serverPlayer, pos.relative(direction));
                }

                if (BlockHandler.isBlockIntractable(block)) {
                    LogBlockEvent.logBlock(serverPlayer, level, state, pos, BlockAction.INTERACT_BLOCK);
                }

                // TODO: mixin to AxeItem to get STRIPPABLES (axe right click event)
                // TODO: hoe event
                // TODO: cauldron
            }
        }
        return InteractionResult.PASS;
    }
}
