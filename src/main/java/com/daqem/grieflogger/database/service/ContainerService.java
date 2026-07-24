package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.command.filter.ActionFilter;
import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.ContainerRepository;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.IAction;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.model.history.IHistory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        Identifier itemLocation = BuiltInRegistries.ITEM.getKey(item.getItem());
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

    public void insertMap(UUID userUuid, Level level, BlockPos pos, Map<ItemAction, List<SimpleItemStack>> itemsMap) {
        containerRepository.insertMap(System.currentTimeMillis(),
                userUuid.toString(),
                level,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                itemsMap);
    }

    public List<IHistory> getHistory(Level level, BlockPos pos) {
        return containerRepository.getHistory(
                level,
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
    }

    public List<IHistory> getHistory(Level level, BlockPos pos, BlockPos connectionPos) {
        return containerRepository.getHistory(
                level,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                connectionPos.getX(),
                connectionPos.getY(),
                connectionPos.getZ()
        );
    }

    public List<IHistory> getFilteredContainerHistory(Level level, FilterList filterList) {
        Optional<ActionFilter> actionFilter = filterList.getActionFilter();
        if ((actionFilter.isPresent() && actionFilter.get().getActions().stream().noneMatch(ContainerService::isValidItemAction))) {
            return List.of();
        }
        return containerRepository.getFilteredContainerHistory(
                level,
                filterList
        );
    }

    private static boolean isValidItemAction(IAction action) {
        return action.equals(ItemAction.ADD_ITEM) || action.equals(ItemAction.REMOVE_ITEM);
    }
}
