package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.service.ItemService;
import com.daqem.grieflogger.event.item.BreakItemEvent;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Runnable;run()V",
            shift = At.Shift.BEFORE
    ), method = "hurtAndBreak(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/Runnable;)V")
    public <T extends LivingEntity> void onHurtAndBreak(int i, RandomSource randomSource, ServerPlayer serverPlayer, Runnable runnable, CallbackInfo ci) {
        BreakItemEvent.breakItem(serverPlayer, (ItemStack) (Object) this);
    }
}
