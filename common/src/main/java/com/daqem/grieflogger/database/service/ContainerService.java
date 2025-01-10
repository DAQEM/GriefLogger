package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.ContainerRepository;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.model.history.IHistory;
import com.daqem.grieflogger.thread.OnComplete;
import com.daqem.grieflogger.thread.ThreadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ContainerService {

    private final ContainerRepository containerRepository;

    public ContainerService(Database database) {
        this.containerRepository = new ContainerRepository(database);
    }

    public void createTable() {
        containerRepository.createTable();
    }

    public void createIndexes() {
        containerRepository.createIndexes();
    }

    public void insert(UUID userUuid, Level level, BlockPos pos, SimpleItemStack item, ItemAction itemAction) {
        ResourceLocation itemLocation = item.getItem().arch$registryName();
        if (itemLocation != null) {
            containerRepository.insert(System.currentTimeMillis(),
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

    public void insertList(UUID userUuid, Level level, BlockPos pos, List<SimpleItemStack> items, ItemAction itemAction) {
        containerRepository.insertList(System.currentTimeMillis(),
                userUuid.toString(),
                level,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                items,
                itemAction.getId());
    }

    public void insertListAsync(UUID uuid, Level level, BlockPos pos, List<SimpleItemStack> items, ItemAction itemAction) {
        ThreadManager.execute(() -> insertList(uuid, level, pos, items, itemAction));
    }

    public void insertMap(UUID userUuid, Level level, BlockPos pos, Map<ItemAction, List<SimpleItemStack>> itemsMap) {
        containerRepository.insertMap(System.currentTimeMillis(),
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

    public List<IHistory> getHistory(Level level, BlockPos pos) {
        return containerRepository.getHistory(
                level,
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
    }

    public void getHistoryAsync(Level level, BlockPos pos, OnComplete<List<IHistory>> onComplete) {
        ThreadManager.submit(() -> getHistory(level, pos), onComplete);
    }

    public List<IHistory> getFilteredContainerHistory(Level level, FilterList filterList) {
        return containerRepository.getFilteredContainerHistory(
                level,
                filterList
        );
    }
}
