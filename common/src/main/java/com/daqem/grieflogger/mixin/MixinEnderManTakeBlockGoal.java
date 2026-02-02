package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal")
public class MixinEnderManTakeBlockGoal {

    @Shadow
    @Final
    private EnderMan enderman;

    @Inject(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onEndermanTakeBlock(CallbackInfo ci, RandomSource randomsource, Level level, int i, int j, int k, BlockPos blockpos, BlockState blockstate, Vec3 vec3, Vec3 vec31, BlockHitResult blockhitresult, boolean flag) {
        EntityUtils.logBlockAction(level, blockpos, blockstate, this.enderman, this.enderman, BlockAction.BREAK_BLOCK);
    }
}