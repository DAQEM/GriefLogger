package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.block.container.ContainerHandler;
import com.daqem.grieflogger.block.container.ContainerTransactionManager;
import com.daqem.grieflogger.command.page.Page;
import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.model.history.BlockHistory;
import com.daqem.grieflogger.model.history.ContainerHistory;
import com.daqem.grieflogger.model.history.IHistory;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements GriefLoggerServerPlayer {

    @Unique
    private boolean grieflogger$inspecting = false;
    @Unique
    private ContainerTransactionManager grieflogger$containerTransactionManager;
    @Unique
    private final Map<ItemAction, List<SimpleItemStack>> grieflogger$itemQueue = new HashMap<>();
    @Unique
    private final List<Page> grieflogger$pages = new ArrayList<>();

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
    public void grieflogger$sendInspectMessage(List<IHistory> historyList) {
        if (historyList.isEmpty()) {
            sendSystemMessage(GriefLogger.translate("lookup.no_history", GriefLogger.getName()));
        } else {
            List<Page> pages = Page.convertToPages(historyList, true);
            grieflogger$setPages(pages);
            Page pageToDisplay = pages.get(0);
            pageToDisplay.sendToPlayer(grieflogger$asServerPlayer());
        }
    }

    @Unique
    public ServerPlayer grieflogger$asServerPlayer() {
        return (ServerPlayer) (Object) this;
    }

    @Override
    public List<Page> grieflogger$getPages() {
        return grieflogger$pages;
    }

    @Override
    public void grieflogger$setPages(List<Page> pages) {
        grieflogger$pages.clear();
        grieflogger$pages.addAll(pages);
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

    @Inject(at = @At("HEAD"), method = "tick")
    public void grieflogger$tick(CallbackInfo ci) {
        if (!grieflogger$itemQueue.isEmpty()) {
            Services.ITEM.insertMapAsync(getUUID(), level(), blockPosition(), new HashMap<>(grieflogger$itemQueue));
            grieflogger$itemQueue.clear();
        }
    }

    public void griefLogger$addItemToQueue(ItemAction action, SimpleItemStack itemStack) {
        List<SimpleItemStack> itemStacks = grieflogger$itemQueue.get(action);
        if (itemStacks != null) {
            SimpleItemStack existingItemStack = itemStacks.stream().filter(itemStack::equals).findFirst().orElse(null);
            if (existingItemStack != null) {
                existingItemStack.setCount(existingItemStack.getCount() + itemStack.getCount());
                return;
            }
        }
        grieflogger$itemQueue.computeIfAbsent(action, k -> new ArrayList<>()).add(itemStack);
    }
}
