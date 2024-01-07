package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.block.BlockHandler;
import com.daqem.grieflogger.database.service.BlockService;
import com.daqem.grieflogger.model.BlockAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class BlockEvents {

    public static void registerEvents() {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            Block block = state.getBlock();
            if (BlockHandler.isBlockIntractable(block)) {
                if (block instanceof DoorBlock) {
                    BlockPos secondPos = null;
                    if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                        secondPos = pos.above();
                    }
                    else if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                        secondPos = pos.below();
                    }
                    if (secondPos != null) {
                        removeInteractionsForPosition(level, secondPos);
                    }
                }
                removeInteractionsForPosition(level, pos);
            }

            return logBlock(player, level, state, pos, BlockAction.BREAK);
        });

        BlockEvent.PLACE.register((level, pos, state, placer) -> {
            if (placer instanceof ServerPlayer player) {
                return logBlock(player, level, state, pos, BlockAction.PLACE);
            }
            return EventResult.pass();
        });

        InteractionEvent.LEFT_CLICK_BLOCK.register((player, hand, pos, direction) ->
                inspectBlock(player, pos));

        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, direction) -> {
            if (hand == InteractionHand.MAIN_HAND) {
                EventResult eventResult = inspectBlock(player, pos.relative(direction));
                if (player instanceof GriefLoggerServerPlayer griefLoggerServerPlayer) {
                    if (!griefLoggerServerPlayer.grieflogger$isInspecting()) {
                        logInteraction(player, pos);
                    }
                }
                return eventResult;
            }
            return EventResult.pass();
        });
    }

    private static void removeInteractionsForPosition(Level level, BlockPos secondPos) {
        BlockService blockService = new BlockService(GriefLogger.getDatabase());
        blockService.removeInteractionsForPositionAsync(level, secondPos);
    }

    private static void logInteraction(Player player, BlockPos pos) {
        BlockState state = player.level().getBlockState(pos);
        ResourceLocation materialLocation = state.getBlock().arch$registryName();
        if (materialLocation != null) {
            BlockService blockService = new BlockService(GriefLogger.getDatabase());
            blockService.insertMaterialAsync(player.getUUID(), player.level().dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ(), materialLocation.toString(), BlockAction.INTERACT);
        }
    }

    private static EventResult logBlock(ServerPlayer player, Level level, BlockState state, BlockPos pos, BlockAction blockAction) {
        ResourceLocation materialLocation = state.getBlock().arch$registryName();
        if (player instanceof GriefLoggerServerPlayer serverPlayer) {
            if (serverPlayer.grieflogger$isInspecting()) {
                return EventResult.interruptFalse();
            }
        }
        if (materialLocation != null) {
            BlockService blockService = new BlockService(GriefLogger.getDatabase());
            blockService.insertMaterialAsync(player.getUUID(), level.dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ(), materialLocation.toString(), blockAction);
        }

        return EventResult.pass();
    }

    private static EventResult inspectBlock(Player player, BlockPos pos) {
        if (player instanceof GriefLoggerServerPlayer serverPlayer) {
            if (serverPlayer.grieflogger$isInspecting()) {
                BlockService blockService = new BlockService(GriefLogger.getDatabase());
                blockService.getHistoryAsync(player.level(), pos, serverPlayer::grieflogger$sendInspectMessage);
                return EventResult.interruptFalse();
            }
        }
        return EventResult.pass();
    }
}
