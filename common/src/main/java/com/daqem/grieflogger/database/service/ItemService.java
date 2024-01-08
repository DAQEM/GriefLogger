package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.ItemRepository;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.thread.ThreadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(Database database) {
        this.itemRepository = new ItemRepository(database);
    }

    public void createTableAsync() {
        ThreadManager.execute(itemRepository::createTable);
    }

    public void insert(UUID userUuid, Level level, BlockPos pos, SimpleItemStack item, ItemAction itemAction) {
        ResourceLocation itemLocation = item.getItem().arch$registryName();
        if (itemLocation != null) {
            itemRepository.insert(System.currentTimeMillis(),
                    userUuid.toString(),
                    level.dimension().location().toString(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    item,
                    itemAction.getId());
        }
    }

    public void insertAsync(UUID userUuid, Level level, BlockPos pos, SimpleItemStack item, ItemAction itemAction) {
        ThreadManager.execute(() -> insert(userUuid, level, pos, item, itemAction));
    }
}
