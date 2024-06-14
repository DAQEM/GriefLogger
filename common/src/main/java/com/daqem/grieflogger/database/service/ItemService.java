package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.ItemRepository;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.model.history.ItemHistory;
import com.daqem.grieflogger.thread.ThreadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(Database database) {
        this.itemRepository = new ItemRepository(database);
    }

    public void createTable() {
        itemRepository.createTable();
    }

    public void createIndexes() {
        itemRepository.createIndexes();
    }

    public void insert(UUID userUuid, Level level, BlockPos pos, SimpleItemStack item, ItemAction itemAction) {
        ResourceLocation itemLocation = item.getItem().arch$registryName();
        if (itemLocation != null) {
            itemRepository.insert(System.currentTimeMillis(),
                    userUuid.toString(),
                    level,
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

    public void insertMap(UUID userUuid, Level level, BlockPos pos, Map<ItemAction, List<SimpleItemStack>> itemsMap) {
        itemRepository.insertMap(System.currentTimeMillis(),
                userUuid.toString(),
                level,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                itemsMap);
    }

    public void insertMapAsync(UUID uuid, Level level, BlockPos pos, Map<ItemAction, List<SimpleItemStack>> itemsMap) {
        ThreadManager.execute(() -> insertMap(uuid, level, pos, itemsMap));
    }

    public List<ItemHistory> getFilteredItemHistory(Level level, FilterList filterList) {
        return itemRepository.getFilteredItemHistory(
                level,
                filterList
        );
    }
}
