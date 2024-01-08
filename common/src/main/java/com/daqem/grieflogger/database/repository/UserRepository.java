package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserRepository {

    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.createTable("""
                CREATE TABLE IF NOT EXISTS users (
                	id integer PRIMARY KEY,
                	name text NOT NULL,
                	uuid text DEFAULT NULL UNIQUE
                );
                """);
    }

    public void insertOrUpdateName(String name, String uuid) {
        String query = """
                INSERT INTO users(name, uuid)
                VALUES(?, ?)
                ON CONFLICT(uuid)
                DO UPDATE SET name = ?
                """;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, uuid);
            preparedStatement.setString(3, name);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert username into database", exception);
        }
    }

    public void insertNonPlayer(String name) {
        String query = """
                INSERT INTO users(name)
                VALUES('%s')
                ON CONFLICT(name)
                DO NOTHING
                """;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert username into database", exception);
        }
    }
}
