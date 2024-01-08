package com.daqem.grieflogger.block.container;

import com.daqem.grieflogger.model.SimpleItemStack;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContainerHandler {

    public static boolean hasContainer(BlockEntity blockEntity) {
        return blockEntity instanceof BaseContainerBlockEntity;
    }

    public static Optional<BaseContainerBlockEntity> getContainer(BlockEntity blockEntity) {
        return hasContainer(blockEntity) ? Optional.of((BaseContainerBlockEntity) blockEntity) : Optional.empty();
    }

    public static boolean hasContainer(MenuProvider menuProvider) {
        return menuProvider instanceof BaseContainerBlockEntity;
    }

    public static Optional<BaseContainerBlockEntity> getContainer(MenuProvider menuProvider) {
        return hasContainer(menuProvider) ? Optional.of((BaseContainerBlockEntity) menuProvider) : Optional.empty();
    }

    public static List<SimpleItemStack> getContainerItems(BaseContainerBlockEntity containerBlockEntity) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (int i = 0; i < containerBlockEntity.getContainerSize(); i++) {
            itemStacks.add(containerBlockEntity.getItem(i));
        }
        return itemStacks.stream()
                .filter(itemStack -> !itemStack.isEmpty() && itemStack.getItem() != Items.AIR)
                .map(SimpleItemStack::new).toList();
    }
}
