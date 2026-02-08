package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.event.item.BreakItemEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(at = @At(value = "HEAD"), method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V")
    private void hurtAndBreak(int i, ServerLevel serverLevel, ServerPlayer serverPlayer, Consumer<Item> consumer, CallbackInfo ci) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.isDamageableItem()) {
            if (serverPlayer == null || !serverPlayer.hasInfiniteMaterials()) {
                if (i > 0) {
                    i = EnchantmentHelper.processDurabilityChange(serverLevel, itemStack, i);
                    if (i <= 0) {
                        return;
                    }
                }

                if (serverPlayer != null && i != 0) {
                    CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, itemStack, itemStack.getDamageValue() + i);
                }

                int j = itemStack.getDamageValue() + i;
                if (j >= itemStack.getMaxDamage()) {
                    BreakItemEvent.breakItem(serverPlayer, itemStack.copyWithCount(1));
                }
            }
        }
    }
}
