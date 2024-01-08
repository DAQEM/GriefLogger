package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LevelRepository {

    private final Database database;

    public LevelRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.createTable("""
                CREATE TABLE IF NOT EXISTS levels (
                	id integer PRIMARY KEY,
                	name text NOT NULL UNIQUE
                );
                """);
    }

    public void insert(String name) {
        String query = """
                INSERT OR IGNORE INTO levels(name)
                VALUES(?);
                """;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert level into database", exception);
        }
    }
}
