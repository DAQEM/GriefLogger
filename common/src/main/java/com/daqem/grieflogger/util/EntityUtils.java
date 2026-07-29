package com.daqem.grieflogger.util;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.action.BlockAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EntityUtils {

    public static void logBlockAction(Level level, BlockPos pos, BlockState state, Entity actor, Entity original, BlockAction action) {
        if (actor == null || original == null || level.isClientSide()) return;

        // Resolve the "Responsible" actor (e.g. resolve TNT owner or Creeper target)
        Entity logicalActor = resolveLogicalActor(actor);

        // Generate the combined Identity
        UUID uuid = getCombinedUUID(logicalActor, original);
        String name = getCombinedName(logicalActor, original);

        Services.USER.insertOrUpdateName(uuid, name);

        Identifier materialLocation = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        Services.BLOCK.insertMaterial(
                uuid,
                level.dimension().identifier().toString(),
                pos,
                materialLocation.toString(),
                action
        );
    }

    private static Entity resolveLogicalActor(Entity entity) {
        if (entity instanceof Player) return entity;
        if (entity instanceof OwnableEntity ownable && ownable.getOwner() != null) return ownable.getOwner();
        if (entity instanceof Projectile projectile && projectile.getOwner() != null) return projectile.getOwner();
        if (entity instanceof Mob mob && mob.getTarget() != null) return mob.getTarget();
        return entity;
    }

    public static UUID getCombinedUUID(Entity actor, Entity original) {
        // If it's just a player doing something directly
        if (actor instanceof Player && original instanceof Player) {
            return actor.getUUID();
        }

        // If it's a Player responsible for an Entity (TNT, Creeper target)
        if (actor instanceof Player && !(original instanceof Player)) {
            String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(original.getType()).toString();
            String combinedId = "link:" + actor.getUUID().toString() + ":" + typeId;
            return UUID.nameUUIDFromBytes(combinedId.getBytes(StandardCharsets.UTF_8));
        }

        // Standard Mob UUID (e.g. pure Enderman or Creeper with no target)
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(actor.getType()).toString();
        return UUID.nameUUIDFromBytes(("entity:" + id).getBytes(StandardCharsets.UTF_8));
    }

    public static String getCombinedName(Entity actor, Entity original) {
        String actorName = actor.getName().getString();
        if (actor instanceof Player && !(original instanceof Player)) {
            String entityName = original.getType().getDescription().getString();
            return actorName + " (" + entityName + ")";
        }
        return actorName;
    }
}