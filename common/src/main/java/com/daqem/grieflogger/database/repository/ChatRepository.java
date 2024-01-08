package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChatRepository {

    private final Database database;

    public ChatRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.createTable("""
                CREATE TABLE IF NOT EXISTS chats (
                    time INTEGER NOT NULL,
                    user INTEGER NOT NULL,
                    level INTEGER NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    message TEXT NOT NULL,
                    FOREIGN KEY(user) REFERENCES users(id),
                    FOREIGN KEY(level) REFERENCES levels(id)
                )
                """);
    }

    public void insert(long time, String userUuid, String levelName, int x, int y, int z, String message) {
        String query = """
                INSERT OR IGNORE INTO chats(time, user, level, x, y, z, message)
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
            preparedStatement.setString(7, message);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert chat into database", exception);
        }
    }
}
