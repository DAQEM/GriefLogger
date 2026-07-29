package com.daqem.grieflogger.event;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.knot.events.EventResult;
import com.daqem.knot.events.EventsService;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public class EntityEvents {

    public static void registerEvents() {
        EventsService.Entity.PLAYER_KILL_ENTITY.register((serverPlayer, entity, damageSource) -> {
            Identifier entityLocation = EntityType.getKey(entity.getType());
            Services.BLOCK.insertEntity(
                    serverPlayer.getUUID(),
                    entity.level().dimension().identifier().toString(),
                    entity.blockPosition(),
                    entityLocation.toString(),
                    BlockAction.KILL_ENTITY
            );
            return EventResult.PASS;
        });
    }
}
