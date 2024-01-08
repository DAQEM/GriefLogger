package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.model.SimpleItemStack;
import net.minecraft.resources.ResourceLocation;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ItemRepository {

    private final Database database;

    public ItemRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.createTable("""
                CREATE TABLE IF NOT EXISTS items (
                    time INTEGER NOT NULL,
                    user INTEGER NOT NULL,
                    level INTEGER NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    type INTEGER NOT NULL,
                    data BLOB DEFAULT NULL,
                    amount INTEGER NOT NULL,
                    action INTEGER NOT NULL,
                    FOREIGN KEY(user) REFERENCES users(id),
                    FOREIGN KEY(level) REFERENCES levels(id),
                    FOREIGN KEY(type) REFERENCES materials(id)
                );
                """);
    }

    public void insert(long time, String userUuid, String levelName, int x, int y, int z, SimpleItemStack item, int action) {
        String query = """
                INSERT OR IGNORE INTO items(time, user, level, x, y, z, type, data, amount, action)
                VALUES(?, (
                    SELECT id FROM users WHERE uuid = ?
                ), (
                    SELECT id FROM levels WHERE name = ?
                ), ?, ?, ?, (
                    SELECT id FROM materials WHERE name = ?
                ), ?, ?, ?);
                """;

        ResourceLocation itemLocation = item.getItem().arch$registryName();
        if (itemLocation != null) {
            try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
                preparedStatement.setLong(1, time);
                preparedStatement.setString(2, userUuid);
                preparedStatement.setString(3, levelName);
                preparedStatement.setInt(4, x);
                preparedStatement.setInt(5, y);
                preparedStatement.setInt(6, z);
                preparedStatement.setString(7, itemLocation.toString());
                preparedStatement.setBytes(8, item.getTagBytes());
                preparedStatement.setInt(9, item.getCount());
                preparedStatement.setInt(10, action);
                preparedStatement.executeUpdate();
            } catch (SQLException exception) {
                GriefLogger.LOGGER.error("Failed to insert item into database", exception);
            }
        }
    }
}
