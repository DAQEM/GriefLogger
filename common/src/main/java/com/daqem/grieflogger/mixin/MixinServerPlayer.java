package com.daqem.grieflogger.mixin;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.block.container.ContainerHandler;
import com.daqem.grieflogger.block.container.ContainerTransactionManager;
import com.daqem.grieflogger.block.container.ContainersTransactionManager;
import com.daqem.grieflogger.block.container.IContainerTransactionManager;
import com.daqem.grieflogger.command.page.Page;
import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.event.item.DropItemEvent;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.model.history.IHistory;
import com.daqem.grieflogger.player.GriefLoggerServerPlayer;
import com.mojang.authlib.GameProfile;
import dev.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    private IContainerTransactionManager grieflogger$containerTransactionManager;
    @Unique
    private final Map<ItemAction, List<SimpleItemStack>> grieflogger$itemQueue = new HashMap<>();
    @Unique
    private final List<Page> grieflogger$pages = new ArrayList<>();

    public MixinServerPlayer(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
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
            if (((Player) this) instanceof ServerPlayer serverPlayer)
                serverPlayer.sendSystemMessage(GriefLogger.translate("lookup.no_history", GriefLogger.getName()));
        } else {
            List<Page> pages = Page.convertToPages(historyList, true);
            grieflogger$setPages(pages);
            Page pageToDisplay = pages.getFirst();
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
        EnvExecutor.getInEnv(EnvType.SERVER, () -> () -> {
            Optional<BaseContainerBlockEntity> container = ContainerHandler.getContainer(menuProvider);
            if (container.isPresent()) {
                this.grieflogger$containerTransactionManager = new ContainerTransactionManager(container.get());
            } else {
                ContainerHandler.getContainers(menuProvider).ifPresent(containers -> {
                    this.grieflogger$containerTransactionManager = new ContainersTransactionManager(containers);
                });
            }
            return null;
        });
    }

    @Inject(at = @At("HEAD"), method = "doCloseContainer()V")
    public void grieflogger$doCloseContainer(CallbackInfo ci) {
        EnvExecutor.getInEnv(EnvType.SERVER, () -> () -> {
            if (this.grieflogger$containerTransactionManager != null) {
                this.grieflogger$containerTransactionManager.finalize(grieflogger$asServerPlayer());
                this.grieflogger$containerTransactionManager = null;
            }
            return null;
        });
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void grieflogger$tick(CallbackInfo ci) {
        EnvExecutor.getInEnv(EnvType.SERVER, () -> () -> {
            if (!grieflogger$itemQueue.isEmpty()) {
                Services.ITEM.insertMap(getUUID(), level(), blockPosition(), new HashMap<>(grieflogger$itemQueue));
                grieflogger$itemQueue.clear();
            }
            return null;
        });
    }

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"))
    private void drop(ItemStack itemStack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> cir) {
        EnvExecutor.getInEnv(EnvType.SERVER, () -> () -> {
            if (cir.getReturnValue() != null) {
                DropItemEvent.onDropItem(this, cir.getReturnValue());
            }
            return null;
        });
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
