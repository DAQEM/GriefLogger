package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.BlockRepository;
import com.daqem.grieflogger.model.BlockAction;
import com.daqem.grieflogger.model.BlockHistory;
import com.daqem.grieflogger.thread.OnComplete;
import com.daqem.grieflogger.thread.ThreadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class BlockService {

    private final BlockRepository blockRepository;

    public BlockService(Database database) {
        this.blockRepository = new BlockRepository(database);
    }

    public void createTableAsync() {
        ThreadManager.execute(blockRepository::createTable);
    }

    public void insertMaterial(UUID userUuid, String levelName, int x, int y, int z, String material, BlockAction blockAction) {
        blockRepository.insertMaterial(System.currentTimeMillis(), userUuid.toString(), levelName, x, y, z, material, blockAction.getId());
    }

    public void insertMaterialAsync(UUID userUuid, String levelName, int x, int y, int z, String material, BlockAction blockAction) {
        ThreadManager.execute(() -> insertMaterial(userUuid, levelName, x, y, z, material, blockAction));
    }

    public void insertEntity(UUID userUuid, String levelName, int x, int y, int z, String entity, BlockAction blockAction) {
        blockRepository.insertEntity(System.currentTimeMillis(), userUuid.toString(), levelName, x, y, z, entity, blockAction.getId());
    }

    public void insertEntityAsync(UUID userUuid, String levelName, int x, int y, int z, String entity, BlockAction blockAction) {
        ThreadManager.execute(() -> insertEntity(userUuid, levelName, x, y, z, entity, blockAction));
    }

    public List<BlockHistory> getHistory(Level level, BlockPos pos) {
        return blockRepository.getHistory(
                level.dimension().location().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
    }

    public void getHistoryAsync(Level level, BlockPos pos, OnComplete<List<BlockHistory>> onComplete) {
        ThreadManager.submit(() -> getHistory(level, pos), onComplete);
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
}
