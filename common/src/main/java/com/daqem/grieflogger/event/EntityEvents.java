package com.daqem.grieflogger.event;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.BlockService;
import com.daqem.grieflogger.model.BlockAction;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class EntityEvents {

    public static void registerEvents() {
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            BlockService blockService = new BlockService(GriefLogger.getDatabase());
            if (source.getEntity() instanceof ServerPlayer serverPlayer) {
                ResourceLocation entityLocation = entity.getType().arch$registryName();
                if (entityLocation != null) {
                    blockService.insertEntityAsync(
                            serverPlayer.getUUID(),
                            entity.level().dimension().location().toString(),
                            entity.blockPosition().getX(),
                            entity.blockPosition().getY(),
                            entity.blockPosition().getZ(),
                            entityLocation.toString(),
                            BlockAction.KILL
                    );
                }
            }
            return EventResult.pass();
        });
    }
}
