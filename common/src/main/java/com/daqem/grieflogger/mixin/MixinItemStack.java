package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.event.item.BreakItemEvent;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(at = @At(value = "HEAD"), method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V")
    private void hurtAndBreak(int amount, ServerLevel level, ServerPlayer player, Consumer<Item> onBreak, CallbackInfo ci) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.isDamageableItem()) {
            if (player == null || !player.hasInfiniteMaterials()) {
                if (amount > 0) {
                    amount = EnchantmentHelper.processDurabilityChange(level, itemStack, amount);
                    if (amount <= 0) {
                        return;
                    }
                }

                if (player != null && amount != 0) {
                    CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(player, itemStack, itemStack.getDamageValue() + amount);
                }

                int j = itemStack.getDamageValue() + amount;
                if (j >= itemStack.getMaxDamage()) {
                    BreakItemEvent.breakItem(player, itemStack.copyWithCount(1));
                }
            }
        }
    }
}
