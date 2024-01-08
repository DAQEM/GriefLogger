package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import net.minecraft.resources.ResourceLocation;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
        if (item.isEmpty()) {
            return;
        }
        String materialQuery = """
                INSERT OR IGNORE INTO materials(name)
                VALUES(?);
                """;

        String itemQuery = """
                INSERT INTO items(time, user, level, x, y, z, type, data, amount, action)
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
            try (PreparedStatement preparedStatement = database.prepareStatement(itemQuery);
                    PreparedStatement materialStatement = database.prepareStatement(materialQuery)) {
                materialStatement.setString(1, itemLocation.toString());
                materialStatement.executeUpdate();

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

    public void insertMap(long time, String userUuid, String levelName, int x, int y, int z, Map<ItemAction, List<SimpleItemStack>> itemsMap) {
        String insertMaterialQuery = """
                INSERT OR IGNORE INTO materials(name)
                VALUES(?);
                """;

        String insertItemQuery = """
                INSERT INTO items(time, user, level, x, y, z, type, data, amount, action)
                VALUES(?, (
                    SELECT id FROM users WHERE uuid = ?
                ), (
                    SELECT id FROM levels WHERE name = ?
                ), ?, ?, ?, (
                    SELECT id FROM materials WHERE name = ?
                ), ?, ?, ?);
                """;

        try (PreparedStatement itemStatement = database.prepareStatement(insertItemQuery);
             PreparedStatement materialStatement = database.prepareStatement(insertMaterialQuery)) {
            for (Map.Entry<ItemAction, List<SimpleItemStack>> entry : itemsMap.entrySet()) {
                for (SimpleItemStack item : entry.getValue()) {
                    if (item.isEmpty()) {
                        continue;
                    }
                    ResourceLocation itemLocation = item.getItem().arch$registryName();
                    if (itemLocation != null) {
                        materialStatement.setString(1, itemLocation.toString());
                        materialStatement.addBatch();

                        itemStatement.setLong(1, time);
                        itemStatement.setString(2, userUuid);
                        itemStatement.setString(3, levelName);
                        itemStatement.setInt(4, x);
                        itemStatement.setInt(5, y);
                        itemStatement.setInt(6, z);
                        itemStatement.setString(7, itemLocation.toString());
                        itemStatement.setBytes(8, item.getTagBytes());
                        itemStatement.setInt(9, item.getCount());
                        itemStatement.setInt(10, entry.getKey().getId());
                        itemStatement.addBatch();
                    }
                }
            }
            materialStatement.executeBatch();
            itemStatement.executeBatch();
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to insert item", e);
        }
    }
}
