package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EntityRepository {

    private final Database database;

    public EntityRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.createTable("""
                CREATE TABLE IF NOT EXISTS entities (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL UNIQUE
                )
                """);
    }

    public void insert(String name) {
        String query = """
                INSERT OR IGNORE INTO entities(name)
                VALUES(?);
                """;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert entity into database", exception);
        }
    }
}
