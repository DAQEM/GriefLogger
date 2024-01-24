package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.MaterialRepository;
import com.daqem.grieflogger.thread.ThreadManager;

public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(Database database) {
        this.materialRepository = new MaterialRepository(database);
    }

    public void createTableAsync() {
        materialRepository.createTable();
    }

    public void insert(String material) {
        materialRepository.insert(material);
    }

    public void insertAsync(String material) {
        ThreadManager.execute(() -> insert(material));
    }
}
