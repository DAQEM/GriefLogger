package com.daqem.grieflogger.database.service;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.repository.UserRepository;
import com.daqem.grieflogger.thread.ThreadManager;

import java.util.Map;
import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;
    private final UsernameService usernameService;

    public UserService(Database database) {
        this.userRepository = new UserRepository(database);
        this.usernameService = new UsernameService(database);
    }

    public void createTable() {
        userRepository.createTable();
    }

    public void insertOrUpdateName(UUID uuid, String name) {
        userRepository.insertOrUpdateName(name,uuid.toString());
        usernameService.insert(uuid, name);
    }

    /**
     * Ensure the player's {@code users} row exists (idempotent, name-carrying) so an event insert's
     * {@code user} foreign key resolves. Lighter than {@link #insertOrUpdateName}: no name rewrite and
     * no username-history row per event — name refreshes still happen at join.
     */
    public void ensure(UUID uuid, String name) {
        userRepository.ensureExists(name, uuid.toString());
    }

    public Map<Integer, String> getAllUsernames() {
        return userRepository.getAllUsernames();
    }
}
