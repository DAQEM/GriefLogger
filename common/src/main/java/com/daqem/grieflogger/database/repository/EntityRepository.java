package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.database.Database;

public class EntityRepository {

    private final Database database;

    public EntityRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.execute("""
                CREATE TABLE IF NOT EXISTS entities (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
                """);
    }

    public void insert(String name) {
        database.executeUpdate("INSERT OR IGNORE INTO entity(name) VALUES('" + name + "')");
    }
}
