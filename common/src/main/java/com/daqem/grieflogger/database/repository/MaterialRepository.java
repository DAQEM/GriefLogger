package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.database.Database;

import java.util.Optional;

public class MaterialRepository {

    private final Database database;

    public MaterialRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.execute("""
                CREATE TABLE IF NOT EXISTS materials (
                	id integer PRIMARY KEY AUTOINCREMENT,
                	name text NOT NULL UNIQUE
                );
                """);
    }

    public void insert(String material) {
        database.executeUpdate("INSERT INTO materials(name) VALUES('" + material + "')");
    }

    public Optional<Integer> getId(String material) {
        String sql = "SELECT id FROM materials WHERE name = '%s'".formatted(material);
        return database.getId(sql);
    }
}
