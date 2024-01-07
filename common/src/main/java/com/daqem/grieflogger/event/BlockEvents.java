package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.BlockService;
import com.daqem.grieflogger.database.service.LevelService;
import com.daqem.grieflogger.database.service.MaterialService;
import com.daqem.grieflogger.database.service.UserService;
import com.daqem.grieflogger.model.Action;
import com.daqem.grieflogger.model.BlockHistory;
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
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BlockEvents {

    public static void registerEvents() {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> logBlock(player, level, state, pos, Action.BREAK));

        BlockEvent.PLACE.register((level, pos, state, placer) -> {
            if (placer instanceof ServerPlayer player) {
                return logBlock(player, level, state, pos, Action.PLACE);
            }
            return EventResult.pass();
        });

        InteractionEvent.LEFT_CLICK_BLOCK.register((player, hand, pos, direction) -> inspectBlock(player, pos));
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, direction) -> {
            if (hand == InteractionHand.MAIN_HAND) {
                return inspectBlock(player, pos.relative(direction));
            }
            return EventResult.pass();
        });
    }

    private static EventResult logBlock(ServerPlayer player, Level level, BlockState state, BlockPos pos, Action action) {
        UserService userService = new UserService(GriefLogger.getDatabase());
        userService.getId(player.getUUID()).ifPresent(userId -> {
            LevelService levelService = new LevelService(GriefLogger.getDatabase());
            levelService.getId(level.dimension().location().toString()).ifPresent(levelId -> {
                MaterialService materialService = new MaterialService(GriefLogger.getDatabase());
                ResourceLocation resourceLocation = state.getBlock().arch$registryName();
                if (resourceLocation != null) {
                    materialService.getOrInsertId(resourceLocation.toString()).ifPresent(materialId -> {
                        BlockService blockService = new BlockService(GriefLogger.getDatabase());
                        blockService.insert(userId, levelId, pos.getX(), pos.getY(), pos.getZ(), materialId, action);
                    });
                }
            });
        });
        return EventResult.pass();
    }

    private static EventResult inspectBlock(Player player, BlockPos pos) {
        if (player instanceof GriefLoggerServerPlayer serverPlayer) {
            if (serverPlayer.grieflogger$isInspecting()) {
                BlockService blockService = new BlockService(GriefLogger.getDatabase());
                List<BlockHistory> history = blockService.getHistory(player.level(), pos);
                serverPlayer.grieflogger$sendInspectMessage(history);
                return EventResult.interruptFalse();
            }
        }
        return EventResult.pass();
    }
}
