package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.LevelRepository;
import com.daqem.grieflogger.thread.ThreadManager;

public class LevelService {

    private final LevelRepository levelRepository;

    public LevelService(Database database) {
        this.levelRepository = new LevelRepository(database);
    }

    public void createTable() {
        levelRepository.createTable();
    }

    public void insert(String name) {
        levelRepository.insert(name);
    }

    public void insertAsync(String name) {
        ThreadManager.execute(() -> insert(name));
    }
}
