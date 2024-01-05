package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.MaterialRepository;

import java.util.Optional;

public class MaterialService {

    private final Database database;
    private final MaterialRepository materialRepository;

    public MaterialService(Database database) {
        this.database = database;
        this.materialRepository = new MaterialRepository(database);
    }

    public void createTable() {
        materialRepository.createTable();
    }

    public void insert(String material) {
        materialRepository.insert(material);
    }

    public Optional<Integer> getOrInsertId(String material) {
        Optional<Integer> id = materialRepository.getId(material);
        if (id.isEmpty()) {
            materialRepository.insert(material);
            id = materialRepository.getId(material);
        }
        return id;
    }
}
