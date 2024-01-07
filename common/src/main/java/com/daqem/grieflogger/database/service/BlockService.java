package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.BlockRepository;
import com.daqem.grieflogger.model.Action;
import com.daqem.grieflogger.model.BlockHistory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

public class BlockService {

    private final Database database;
    private final BlockRepository blockRepository;

    public BlockService(Database database) {
        this.database = database;
        this.blockRepository = new BlockRepository(database);
    }

    public void createTable() {
        blockRepository.createTable();
    }

    public void insert(int user, int level, int x, int y, int z, int material, Action action) {
        blockRepository.insert(System.currentTimeMillis(), user, level, x, y, z, material, action.getId());
    }

    public List<BlockHistory> getHistory(Level level, BlockPos pos) {
        return blockRepository.getHistory(
                level.dimension().location().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
    }
}
