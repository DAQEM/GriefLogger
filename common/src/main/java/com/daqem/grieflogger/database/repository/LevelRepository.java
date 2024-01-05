package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.database.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class LevelRepository {

    private final Database database;

    public LevelRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.execute("""
                CREATE TABLE IF NOT EXISTS levels (
                	id integer PRIMARY KEY AUTOINCREMENT,
                	name text NOT NULL UNIQUE
                );
                """);
    }

    public void insert(String name) {
        database.executeUpdate("INSERT INTO levels(name) VALUES('" + name + "') ON CONFLICT(name) DO NOTHING");
    }

    public Optional<Integer> getId(String string) {
        String sql = "SELECT id FROM levels WHERE name = '%s'".formatted(string);
        return database.getId(sql);
    }
}
