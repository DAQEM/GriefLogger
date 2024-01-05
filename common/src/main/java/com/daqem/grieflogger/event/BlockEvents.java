package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.BlockService;
import com.daqem.grieflogger.database.service.LevelService;
import com.daqem.grieflogger.database.service.MaterialService;
import com.daqem.grieflogger.database.service.UserService;
import com.daqem.grieflogger.model.Action;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEvents {

    public static void registerEvents() {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            logBlockEvent(player, level, state, pos, Action.BREAK);
            return EventResult.pass();
        });

        BlockEvent.PLACE.register((level, pos, state, placer) -> {
            if (placer instanceof ServerPlayer player) {
                logBlockEvent(player, level, state, pos, Action.PLACE);
            }
            return EventResult.pass();
        });
    }

    private static void logBlockEvent(ServerPlayer player, Level level, BlockState state, BlockPos pos, Action action) {
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
    }
}
