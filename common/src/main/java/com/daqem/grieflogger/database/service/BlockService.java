package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.BlockRepository;
import com.daqem.grieflogger.model.action.BlockAction;
import com.daqem.grieflogger.model.history.BlockHistory;
import com.daqem.grieflogger.model.history.IHistory;
import com.daqem.grieflogger.thread.OnComplete;
import com.daqem.grieflogger.thread.ThreadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlockService {

    private final BlockRepository blockRepository;

    public BlockService(Database database) {
        this.blockRepository = new BlockRepository(database);
    }

    public void createTableAsync() {
        blockRepository.createTable();
    }

    public void insertMaterial(UUID userUuid, String levelName, BlockPos pos, String material, BlockAction blockAction) {
        material = material.replace("minecraft:", "");
        blockRepository.insertMaterial(System.currentTimeMillis(), userUuid.toString(), levelName, pos.getX(), pos.getY(), pos.getZ(), material, blockAction.getId());
    }

    public void insertMaterialAsync(UUID userUuid, String levelName, BlockPos pos, String material, BlockAction blockAction) {
        ThreadManager.execute(() -> insertMaterial(userUuid, levelName, pos, material, blockAction));
    }

    public void insertEntity(UUID userUuid, String levelName, BlockPos pos, String entity, BlockAction blockAction) {
        entity = entity.replace("minecraft:", "");
        blockRepository.insertEntity(System.currentTimeMillis(), userUuid.toString(), levelName, pos.getX(), pos.getY(), pos.getZ(), entity, blockAction.getId());
    }

    public void insertEntityAsync(UUID userUuid, String levelName, BlockPos pos, String entity, BlockAction blockAction) {
        ThreadManager.execute(() -> insertEntity(userUuid, levelName, pos, entity, blockAction));
    }

    public List<IHistory> getBlockHistory(Level level, BlockPos pos) {
        return blockRepository.getBlockHistory(
                level.dimension().location().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
    }

    public void getBlockHistoryAsync(Level level, BlockPos pos, OnComplete<List<IHistory>> onComplete) {
        ThreadManager.submit(() -> getBlockHistory(level, pos), onComplete);
    }

    public List<IHistory> getBlockHistory(Level level, List<BlockPos> pos) {
        List<IHistory> blockHistories = new ArrayList<>();

        for (BlockPos blockPos : pos) {
            blockHistories.addAll(getBlockHistory(level, blockPos));
        }

        return blockHistories.stream()
                .sorted((o1, o2) -> (int) (o2.getTime().time() - o1.getTime().time()))
                .toList();
    }

    public void getBlockHistoryAsync(Level level, List<BlockPos> pos, OnComplete<List<IHistory>> onComplete) {
        ThreadManager.submit(() -> getBlockHistory(level, pos), onComplete);
    }

    public List<IHistory> getInteractionHistory(Level level, BlockPos pos) {
        return blockRepository.getInteractionHistory(
                level.dimension().location().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
    }

    public void getInteractionHistoryAsync(Level level, BlockPos pos, OnComplete<List<IHistory>> onComplete) {
        ThreadManager.submit(() -> getBlockHistory(level, pos), onComplete);
    }

    public List<IHistory> getInteractionHistory(Level level, List<BlockPos> pos) {
        List<IHistory> blockHistories = new ArrayList<>();

        for (BlockPos blockPos : pos) {
            blockHistories.addAll(getInteractionHistory(level, blockPos));
        }

        return blockHistories.stream()
                .sorted((o1, o2) -> (int) (o2.getTime().time() - o1.getTime().time()))
                .toList();
    }

    public void getInteractionHistoryAsync(Level level, List<BlockPos> pos, OnComplete<List<IHistory>> onComplete) {
        ThreadManager.submit(() -> getInteractionHistory(level, pos), onComplete);
    }

    public void removeInteractionsForPosition(Level level, BlockPos secondPos) {
        blockRepository.removeInteractionsForPosition(
                level.dimension().location().toString(),
                secondPos.getX(),
                secondPos.getY(),
                secondPos.getZ()
        );
    }

    public void removeInteractionsForPositionAsync(Level level, BlockPos secondPos) {
        ThreadManager.execute(() -> removeInteractionsForPosition(level, secondPos));
    }

    public List<IHistory> getFilteredBlockHistory(Level level, FilterList filterList) {
        return blockRepository.getFilteredBlockHistory(
                level.dimension().location().toString(),
                filterList
        );
    }
}
