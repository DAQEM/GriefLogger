package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.database.Database;

import java.util.Optional;

public class UserRepository {

    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.execute("""
                CREATE TABLE IF NOT EXISTS users (
                	id integer PRIMARY KEY AUTOINCREMENT,
                	name text NOT NULL,
                	uuid text DEFAULT NULL UNIQUE
                );
                """);
    }

    public void insertOrUpdateName(String name, String uuid) {
        database.executeUpdate("""
                INSERT INTO users(name, uuid)
                VALUES('%s', '%s')
                ON CONFLICT(uuid)
                DO UPDATE SET name = '%s'
                """.formatted(name, uuid, name));
    }

    public void insertNonPlayer(String name) {
        database.executeUpdate("""
                INSERT INTO users(name)
                VALUES('%s')
                ON CONFLICT(name)
                DO NOTHING
                """.formatted(name));
    }

    public Optional<Integer> getId(String uuid) {
        String sql = "SELECT id FROM users WHERE uuid = '%s'".formatted(uuid);
        return database.getId(sql);
    }

    public Optional<Integer> getNonPlayerId(String name) {
        String sql = "SELECT id FROM users WHERE name = '%s'".formatted(name);
        return database.getId(sql);
    }
}
