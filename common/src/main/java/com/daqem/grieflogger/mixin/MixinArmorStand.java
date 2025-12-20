package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class MixinArmorStand extends LivingEntity {

    protected MixinArmorStand(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "interactAt", at = @At("RETURN"))
    private void onInteractAt(Player player, Vec3 vec3, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        if ((cir.getReturnValue() == InteractionResult.SUCCESS || cir.getReturnValue() == InteractionResult.SUCCESS_SERVER) && player instanceof GriefLoggerServerPlayer glPlayer) {
            Identifier entityLocation = this.getType().arch$registryName();
            if (entityLocation != null) {
                Services.BLOCK.insertEntity(
                        glPlayer.grieflogger$asServerPlayer().getUUID(),
                        this.level().dimension().identifier().toString(),
                        this.blockPosition(),
                        entityLocation.toString(),
                        BlockAction.INTERACT_ENTITY
                );
            }
        }
    }
}
