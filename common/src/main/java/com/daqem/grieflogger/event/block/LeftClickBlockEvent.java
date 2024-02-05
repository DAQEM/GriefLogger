package com.daqem.grieflogger.event.block;

import com.daqem.grieflogger.event.AbstractEvent;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LeftClickBlockEvent extends AbstractEvent {

    public static EventResult leftClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction direction) {
        if (player instanceof GriefLoggerServerPlayer serverPlayer) {
            if (hand == InteractionHand.MAIN_HAND) {
                if (serverPlayer.grieflogger$isInspecting()) {

                    Level level = player.getLevel();
                    BlockState state = level.getBlockState(pos);
                    Block block = state.getBlock();

                    if (block instanceof DoorBlock) {
                        return InspectDoorEvent.inspectDoor(serverPlayer, level, pos, state, false);
                    }
                    return InspectBlockEvent.inspectBlock(serverPlayer, pos);
                }
            }
        }
        return pass();
    }
}
