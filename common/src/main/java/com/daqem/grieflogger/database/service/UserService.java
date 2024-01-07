package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.UserRepository;
import com.daqem.grieflogger.thread.ThreadManager;

import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;
    private final UsernameService usernameService;

    public UserService(Database database) {
        this.userRepository = new UserRepository(database);
        this.usernameService = new UsernameService(database);
    }

    public void createTableAsync() {
        ThreadManager.execute(userRepository::createTable);
    }

    public void insertOrUpdateName(UUID uuid, String name) {
        userRepository.insertOrUpdateName(name,uuid.toString());
        usernameService.insert(uuid, name);
    }

    public void insertOrUpdateNameAsync(UUID uuid, String name) {
        ThreadManager.execute(() -> insertOrUpdateName(uuid, name));
    }
}
