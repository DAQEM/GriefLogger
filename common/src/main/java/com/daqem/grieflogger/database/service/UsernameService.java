package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.UsernameRepository;

import java.util.Optional;
import java.util.UUID;

public class UsernameService {

    private final Database database;
    private final UsernameRepository usernameRepository;

    public UsernameService(Database database) {
        this.database = database;
        this.usernameRepository = new UsernameRepository(database);
    }

    public void createTable() {
        usernameRepository.createTable();
    }

    public void insert(UUID uuid, String name) {
        usernameRepository.insert(System.currentTimeMillis(), uuid.toString(), name);
    }

    public Optional<String> getName(UUID uuid) {
        return usernameRepository.getName(uuid.toString());
    }
}
