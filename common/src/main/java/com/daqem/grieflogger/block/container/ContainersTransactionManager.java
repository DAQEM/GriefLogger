package com.daqem.grieflogger.block.container;

import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainersTransactionManager implements IContainerTransactionManager {

    private final List<BaseContainerBlockEntity> blockEntities;

    private final Map<BaseContainerBlockEntity, List<SimpleItemStack>> initialItems = new HashMap<>();
    private final Map<BaseContainerBlockEntity, List<SimpleItemStack>> finalItems = new HashMap<>();

    public ContainersTransactionManager(List<BaseContainerBlockEntity> blockEntities) {
        this.blockEntities = blockEntities;
        for (BaseContainerBlockEntity blockEntity : blockEntities) {
            initialItems.put(blockEntity, new ArrayList<>());
            finalItems.put(blockEntity, new ArrayList<>());
            for (int i = 0; i < blockEntity.getContainerSize(); i++) {
                addItem(blockEntity.getItem(i), initialItems.get(blockEntity));
            }
        }
    }

    public void finalize(ServerPlayer serverPlayer) {
        for (BaseContainerBlockEntity blockEntity : blockEntities) {
            constructFinalItems(blockEntity);

            List<SimpleItemStack> removedItems = getRemovedItems(blockEntity);
            List<SimpleItemStack> addedItems = getAddedItems(blockEntity);

            Services.CONTAINER.insertMap(
                    serverPlayer.getUUID(),
                    blockEntity.getLevel() != null ? blockEntity.getLevel() : serverPlayer.level(),
                    blockEntity.getBlockPos(),
                    Map.of(
                            ItemAction.REMOVE_ITEM, removedItems,
                            ItemAction.ADD_ITEM, addedItems
                    )
            );
        }

    }

    private void constructFinalItems(BaseContainerBlockEntity blockEntity) {
        for (int i = 0; i < blockEntity.getContainerSize(); i++) {
            addItem(blockEntity.getItem(i), finalItems.get(blockEntity));
        }
    }

    private List<SimpleItemStack> getRemovedItems(BaseContainerBlockEntity blockEntity) {
        return new ArrayList<>(getDifference(initialItems.get(blockEntity), finalItems.get(blockEntity)));
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

    private List<SimpleItemStack> getAddedItems(BaseContainerBlockEntity blockEntity) {
        return new ArrayList<>(getDifference(finalItems.get(blockEntity), initialItems.get(blockEntity)));
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
