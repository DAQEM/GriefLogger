package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.event.item.BreakItemEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V",
        shift = At.Shift.BEFORE
    ), method = "applyDamage(ILnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V")
    private void applyDamage(int i, @Nullable ServerPlayer serverPlayer, Consumer<Item> consumer, CallbackInfo ci)
    {
        BreakItemEvent.breakItem(serverPlayer, (ItemStack) (Object) this);
    }
}
