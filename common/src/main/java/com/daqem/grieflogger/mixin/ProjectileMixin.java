package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.ItemService;
import com.daqem.grieflogger.event.item.ShootItemEvent;
import com.daqem.grieflogger.event.item.ThrowItemEvent;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public class ProjectileMixin {

    @Inject(at = @At("HEAD"), method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V")
    private void shootFromRotation(Entity entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (entity instanceof Player player) {
            if ((Projectile) (Object) this instanceof ThrowableItemProjectile throwableItemProjectile) {
                ThrowItemEvent.throwItem(player, throwableItemProjectile.getItem());
            } else if ((Projectile) (Object) this instanceof AbstractArrow abstractArrow) {
                ShootItemEvent.shootItem(player, abstractArrow.getPickupItem());
            }
        }
    }
}
