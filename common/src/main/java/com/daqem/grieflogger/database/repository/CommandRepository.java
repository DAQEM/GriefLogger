package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CommandRepository {

    private final Database database;

    public CommandRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.createTable("""
                CREATE TABLE IF NOT EXISTS commands (
                    time INTEGER NOT NULL,
                    user INTEGER NOT NULL,
                    level INTEGER NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    command TEXT NOT NULL,
                    FOREIGN KEY(user) REFERENCES users(id),
                    FOREIGN KEY(level) REFERENCES levels(id)
                )
                """);
    }

    public void insert(long time, String userUuid, String levelName, int x, int y, int z, String command) {
        String query = """
                INSERT OR IGNORE INTO commands(time, user, level, x, y, z, command)
                VALUES(?, (
                    SELECT id FROM users WHERE uuid = ?
                ), (
                    SELECT id FROM levels WHERE name = ?
                ), ?, ?, ?, ?);
                """;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setLong(1, time);
            preparedStatement.setString(2, userUuid);
            preparedStatement.setString(3, levelName);
            preparedStatement.setInt(4, x);
            preparedStatement.setInt(5, y);
            preparedStatement.setInt(6, z);
            preparedStatement.setString(7, command);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert command into database", exception);
        }
    }
}