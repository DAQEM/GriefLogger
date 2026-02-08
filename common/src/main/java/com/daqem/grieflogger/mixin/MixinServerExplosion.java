package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerExplosion.class)
public abstract class MixinServerExplosion {

    @Shadow
    @Final
    private @Nullable Entity source;

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow public abstract @Nullable LivingEntity getIndirectSourceEntity();

    @Inject(method = "interactWithBlocks", at = @At("HEAD"))
    private void onInteractWithBlocks(List<BlockPos> list, CallbackInfo ci) {
        // actor = Who to blame (Player or the Mob itself)
        Entity actor = this.getIndirectSourceEntity();
        if (actor == null) actor = this.source;

        // original = The tool used (TNT, Creeper, etc.)
        Entity original = this.source;

        if (actor == null || original == null) return;

        for (BlockPos pos : list) {
            BlockState state = this.level.getBlockState(pos);
            if (!state.isAir()) {
                // Pass both to our new utility
                EntityUtils.logBlockAction(this.level, pos, state, actor, original, BlockAction.BREAK_BLOCK);
            }
        }
    }
}