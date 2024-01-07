package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.UsernameRepository;
import com.daqem.grieflogger.thread.ThreadManager;

import java.util.UUID;

public class UsernameService {

    private final UsernameRepository usernameRepository;

    public UsernameService(Database database) {
        this.usernameRepository = new UsernameRepository(database);
    }

    public void createTableAsync() {
        ThreadManager.execute(usernameRepository::createTable);
    }

    public void insert(UUID uuid, String name) {
        ThreadManager.execute(() -> usernameRepository.insert(System.currentTimeMillis(), uuid.toString(), name));
    }

    public void insertAsync(UUID uuid, String name) {
        ThreadManager.execute(() -> insert(uuid, name));
    }
}
