package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.EntityRepository;
import com.daqem.grieflogger.thread.ThreadManager;

public class EntityService {

    private final EntityRepository entityRepository;

    public EntityService(Database database) {
        this.entityRepository = new EntityRepository(database);
    }

    public void createTableAsync() {
        ThreadManager.execute(entityRepository::createTable);
    }

    public void insert(String name) {
        entityRepository.insert(name);
    }

    public void insertAsync(String name) {
        ThreadManager.execute(() -> insert(name));
    }
}
