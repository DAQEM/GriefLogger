package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.util.EntityUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class MixinServerExplosion {
    @Shadow @Final private Level level;
    @Shadow @Nullable public Entity source;
    @Shadow @Final private ObjectArrayList<BlockPos> toBlow;

    @Shadow public abstract @Nullable LivingEntity getIndirectSourceEntity();

    @Inject(method = "finalizeExplosion", at = @At("HEAD"))
    private void onInteractWithBlocks(boolean spawnParticles, CallbackInfo ci) {
        if (!(this.level instanceof ServerLevel serverLevel)) return;

        // actor = Who to blame (Player or the Mob itself)
        Entity actor = this.getIndirectSourceEntity();
        if (actor == null) actor = this.source;

        // original = The tool used (TNT, Creeper, etc.)
        Entity original = this.source;

        if (actor == null || original == null) return;

        for (BlockPos pos : this.toBlow) {
            BlockState state = serverLevel.getBlockState(pos);
            if (!state.isAir()) {
                // Pass both to our new utility
                EntityUtils.logBlockAction(serverLevel, pos, state, actor, original, BlockAction.BREAK_BLOCK);
            }
        }
    }
}