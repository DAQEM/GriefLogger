package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.block.container.ContainerHandler;
import com.daqem.grieflogger.block.container.ContainerTransactionManager;
import com.daqem.grieflogger.model.history.BlockHistory;
import com.daqem.grieflogger.model.history.ContainerHistory;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements GriefLoggerServerPlayer {

    @Shadow @Final public MinecraftServer server;
    @Unique
    private boolean grieflogger$inspecting = false;
    @Unique
    private ContainerTransactionManager grieflogger$containerTransactionManager;

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
    public void grieflogger$sendBlockInspectMessage(List<BlockHistory> blockHistory) {
        if (blockHistory.isEmpty()) {
            sendSystemMessage(GriefLogger.translate("inspect.no_history", GriefLogger.getName()));
        } else {
            sendSystemMessage(GriefLogger.translate("inspect.block.history_header",
                    GriefLogger.themedTranslate("inspect.block.history_title"),
                    blockHistory.get(0).position().getComponent()
            ));
            for (BlockHistory history : blockHistory) {
                sendSystemMessage(
                        GriefLogger.translate("inspect.block.history_entry",
                                history.time().getFormattedTimeAgo(),
                                history.action().getPrefix(),
                                history.user().getNameComponent(),
                                history.action().getPastTense(),
                                history.getMaterialComponent()
                        ));
            }
        }
    }

    @Unique
    public void grieflogger$sendContainerInspectMessage(List<ContainerHistory> containerHistory) {
        if (containerHistory.isEmpty()) {
            sendSystemMessage(GriefLogger.translate("inspect.no_history", GriefLogger.getName()));
        } else {
            sendSystemMessage(GriefLogger.translate("inspect.container.history_header",
                    GriefLogger.themedTranslate("inspect.container.history_title"),
                    containerHistory.get(0).position().getComponent()
            ));
            for (ContainerHistory history : containerHistory) {
                sendSystemMessage(
                        GriefLogger.translate("inspect.container.history_entry",
                                history.time().getFormattedTimeAgo(),
                                history.action().getPrefix(),
                                history.user().getNameComponent(),
                                history.action().getPastTense(),
                                history.itemStack().getCount(),
                                history.getMaterialComponent()
                        ));
            }
        }
    }

    @Unique
    public ServerPlayer grieflogger$asServerPlayer() {
        return (ServerPlayer) (Object) this;
    }

    @Inject(at = @At("HEAD"), method = "openMenu")
    public void openMenu(MenuProvider menuProvider, CallbackInfoReturnable<OptionalInt> cir) {
        ContainerHandler.getContainer(menuProvider).ifPresent(container -> {
            this.grieflogger$containerTransactionManager = new ContainerTransactionManager(container);
        });
    }

    @Inject(at = @At("HEAD"), method = "doCloseContainer()V")
    public void grieflogger$doCloseContainer(CallbackInfo ci) {
        if (this.grieflogger$containerTransactionManager != null) {
            this.grieflogger$containerTransactionManager.finalize(grieflogger$asServerPlayer());
            this.grieflogger$containerTransactionManager = null;
        }
    }
}
