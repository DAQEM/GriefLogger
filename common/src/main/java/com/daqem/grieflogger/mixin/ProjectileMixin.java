package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.event.item.ShootItemEvent;
import com.daqem.grieflogger.event.item.ThrowItemEvent;
import com.daqem.knot.api.world.entity.IAbstractArrow;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public class ProjectileMixin {

    @Inject(at = @At("HEAD"), method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V")
    private void shootFromRotation(Entity source, float xRot, float yRot, float yOffset, float pow, float uncertainty, CallbackInfo ci) {
        if (source instanceof Player player) {
            if ((Projectile) (Object) this instanceof ThrowableItemProjectile throwableItemProjectile) {
                ThrowItemEvent.throwItem(player, throwableItemProjectile.getItem());
            } else if ((Projectile) (Object) this instanceof IAbstractArrow abstractArrow) {
                ShootItemEvent.shootItem(player, abstractArrow.knot$getPickupItem());
            }
        }
    }
}
