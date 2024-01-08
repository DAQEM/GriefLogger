package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.ItemService;
import com.daqem.grieflogger.event.item.BreakItemEvent;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    ), method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V")
    public <T extends LivingEntity> void onHurtAndBreak(int i, T livingEntity, Consumer<T> consumer, CallbackInfo ci) {
        if (livingEntity instanceof Player player) {
            BreakItemEvent.breakItem(player, (ItemStack) (Object) this);
        }
    }
}
