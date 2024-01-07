package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.model.BlockHistory;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements GriefLoggerServerPlayer {

    @Unique
    private boolean grieflogger$inspecting = false;

    public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Unique
    public boolean grieflogger$isInspecting() {
        return grieflogger$inspecting;
    }

    @Unique
    public void grieflogger$setInspecting(boolean inspecting) {
        this.grieflogger$inspecting = inspecting;
    }

    @Unique
    public void grieflogger$sendInspectMessage(List<BlockHistory> blockHistory) {
        if (blockHistory.isEmpty()) {
            sendSystemMessage(GriefLogger.translate("inspect.no_history", GriefLogger.getName()));
        } else {
            sendSystemMessage(GriefLogger.translate("inspect.history_header",
                    GriefLogger.getName(),
                    blockHistory.get(0).position().getComponent()
            ));
            for (BlockHistory history : blockHistory) {
                sendSystemMessage(
                        GriefLogger.translate("inspect.history_entry",
                                history.time().getFormattedTimeAgo(),
                                history.blockAction().getPrefix(),
                                history.user().getNameComponent(),
                                history.blockAction().getPastTense(),
                                history.getMaterialComponent()
                        ));
            }
        }
    }
}
