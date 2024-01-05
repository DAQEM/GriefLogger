package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.BlockRepository;
import com.daqem.grieflogger.model.Action;

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
}
