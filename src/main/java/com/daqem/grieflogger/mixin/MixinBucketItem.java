package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.event.block.BreakBlockEvent;
import com.daqem.grieflogger.event.block.PlaceBlockEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class MixinBucketItem {

    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;)V",
                    ordinal = 0
            )
    )
    private void onBucketFilled(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir, @Local(ordinal = 1) ItemStack itemStack2, @Local BlockHitResult blockHitResult) {
        if (player instanceof ServerPlayer serverPlayer && itemStack2.getItem() instanceof BucketItem bucketItem) {
            BreakBlockEvent.breakBlock(level, blockHitResult.getBlockPos(), bucketItem.getContent().defaultFluidState().createLegacyBlock(), serverPlayer);
        }
    }
}