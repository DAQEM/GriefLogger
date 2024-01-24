package com.daqem.grieflogger.block.container;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContainerTransactionManager {

    private final BaseContainerBlockEntity blockEntity;

    private final List<SimpleItemStack> initialItems = new ArrayList<>();
    private final List<SimpleItemStack> finalItems = new ArrayList<>();

    public ContainerTransactionManager(BaseContainerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        for (int i = 0; i < blockEntity.getContainerSize(); i++) {
            addItem(blockEntity.getItem(i), initialItems);
        }
    }

    public void finalize(ServerPlayer serverPlayer) {
        constructFinalItems();
        List<SimpleItemStack> removedItems = getRemovedItems();
        List<SimpleItemStack> addedItems = getAddedItems();

        Services.CONTAINER.insertMapAsync(
                serverPlayer.getUUID(),
                blockEntity.getLevel() != null ? blockEntity.getLevel() : serverPlayer.level(),
                blockEntity.getBlockPos(),
                Map.of(
                        ItemAction.REMOVE_ITEM, removedItems,
                        ItemAction.ADD_ITEM, addedItems
                )
        );
    }

    private void constructFinalItems() {
        for (int i = 0; i < blockEntity.getContainerSize(); i++) {
            addItem(blockEntity.getItem(i), finalItems);
        }
    }

    private List<SimpleItemStack> getRemovedItems() {
        return new ArrayList<>(getDifference(initialItems, finalItems));
    }

    private List<SimpleItemStack> getDifference(List<SimpleItemStack> x, List<SimpleItemStack> y) {
        List<SimpleItemStack> difference = new ArrayList<>();
        for (SimpleItemStack xItem : x) {
            y.stream().filter(xItem::equals).findFirst().ifPresentOrElse(yItem -> {
                if (yItem.getCount() < xItem.getCount()) {
                    difference.add(new SimpleItemStack(xItem.getItem(), xItem.getCount() - yItem.getCount(), xItem.getTag()));
                }
            }, () -> difference.add(xItem));
        }
        return difference;
    }

    private List<SimpleItemStack> getAddedItems() {
        return new ArrayList<>(getDifference(finalItems, initialItems));
    }

    private void addItem(ItemStack itemStack, List<SimpleItemStack> itemStackList) {
        if (itemStack.getItem().equals(Items.AIR)) {
            return;
        }

        if (itemStack.getCount() == 0) {
            return;
        }

        for (SimpleItemStack simpleItemStack : itemStackList) {
            if (simpleItemStack.getItem() == itemStack.getItem()) {
                if (simpleItemStack.hasTag() && itemStack.hasTag() && simpleItemStack.getTag().equals(itemStack.getTag())) {
                    simpleItemStack.addCount(itemStack.getCount());
                    return;
                } else if (simpleItemStack.hasNoTag() && !itemStack.hasTag()) {
                    simpleItemStack.addCount(itemStack.getCount());
                    return;
                }
            }
        }

        itemStackList.add(new SimpleItemStack(itemStack));
    }
}
