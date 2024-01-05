package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.LevelRepository;

import java.util.Optional;

public class LevelService {

    private final Database database;
    private final LevelRepository levelRepository;

    public LevelService(Database database) {
        this.database = database;
        this.levelRepository = new LevelRepository(database);
    }

    public void createTable() {
        levelRepository.createTable();
    }

    public void insert(String name) {
        levelRepository.insert(name);
    }

    public Optional<Integer> getId(String string) {
        return levelRepository.getId(string);
    }
}
