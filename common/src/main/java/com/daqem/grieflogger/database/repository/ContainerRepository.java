package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.model.history.ContainerHistory;
import net.minecraft.resources.ResourceLocation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContainerRepository {

    private final Database database;

    public ContainerRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.createTable("""
                CREATE TABLE IF NOT EXISTS containers (
                    time integer NOT NULL,
                	user integer NOT NULL,
                	level integer NOT NULL,
                	x integer NOT NULL,
                	y integer NOT NULL,
                	z integer NOT NULL,
                	type integer NOT NULL,
                	data blob DEFAULT NULL,
                	amount integer NOT NULL,
                	action integer NOT NULL,
                	FOREIGN KEY(user) REFERENCES users(id),
                	FOREIGN KEY(level) REFERENCES levels(id),
                	FOREIGN KEY(type) REFERENCES materials(id)
                )
                """);
    }

    public void insert(long time, String userUuid, String levelName, int x, int y, int z, SimpleItemStack item, int itemAction) {
        if (item.isEmpty()) {
            return;
        }

        String insertMaterialQuery = """
                INSERT OR IGNORE INTO materials(name)
                VALUES(?);
                """;
        String insertItemQuery = """
                INSERT INTO containers(time, user, level, x, y, z, type, data, amount, action)
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
        try (PreparedStatement itemStatement = database.prepareStatement(insertItemQuery);
             PreparedStatement materialStatement = database.prepareStatement(insertMaterialQuery)) {

                materialStatement.setString(1, itemLocation.toString());
                materialStatement.execute();

                itemStatement.setLong(1, time);
                itemStatement.setString(2, userUuid);
                itemStatement.setString(3, levelName);
                itemStatement.setInt(4, x);
                itemStatement.setInt(5, y);
                itemStatement.setInt(6, z);
                itemStatement.setString(7, itemLocation.toString());
                itemStatement.setBytes(8, item.getTagBytes());
                itemStatement.setInt(9, item.getCount());
                itemStatement.setInt(10, itemAction);
                itemStatement.execute();
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to insert item", e);
        }
        }
    }

    public void insertList(long time, String userUuid, String levelName, int x, int y, int z, List<SimpleItemStack> items, int itemAction) {
        String insertMaterialQuery = """
                INSERT OR IGNORE INTO materials(name)
                VALUES(?);
                """;
        String insertItemQuery = """
                INSERT INTO containers(time, user, level, x, y, z, type, data, amount, action)
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
            for (SimpleItemStack item : items) {
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
                    itemStatement.setInt(10, itemAction);
                    itemStatement.addBatch();
                }
            }
            materialStatement.executeBatch();
            itemStatement.executeBatch();
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to insert item", e);
        }
    }

    public void insertMap(long time, String userUuid, String levelName, int x, int y, int z, Map<ItemAction, List<SimpleItemStack>> itemsMap) {
        String insertMaterialQuery = """
                INSERT OR IGNORE INTO materials(name)
                VALUES(?);
                """;
        String insertItemQuery = """
                INSERT INTO containers(time, user, level, x, y, z, type, data, amount, action)
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

    public List<ContainerHistory> getHistory(String levelName, int x, int y, int z) {
        List<ContainerHistory> containerHistory = new ArrayList<>();
        String query = """
                SELECT containers.time, users.name, users.uuid, containers.x, containers.y, containers.z, materials.name, containers.data, containers.amount, containers.action
                FROM containers
                INNER JOIN users ON containers.user = users.id
                INNER JOIN levels ON containers.level = (
                    SELECT id FROM levels WHERE name = ?
                )
                INNER JOIN materials ON containers.type = materials.id
                WHERE containers.level = levels.id AND containers.x = ? AND containers.y = ? AND containers.z = ? AND (containers.action = 0 OR containers.action = 1)
                ORDER BY containers.time DESC
                """;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, levelName);
            preparedStatement.setInt(2, x);
            preparedStatement.setInt(3, y);
            preparedStatement.setInt(4, z);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                containerHistory.add(new ContainerHistory(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getInt(4),
                        resultSet.getInt(5),
                        resultSet.getInt(6),
                        resultSet.getString(7),
                        resultSet.getBytes(8),
                        resultSet.getInt(9),
                        resultSet.getInt(10)
                ));
            }
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to get container history", e);
        }
        return containerHistory;
    }
}
