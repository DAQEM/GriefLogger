package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.dialect.MySQLDialect;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.model.history.ItemHistory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemRepository extends Repository {

    private final Database database;

    public ItemRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS items (" +
                "time " + database.getDialect().getDataType("bigint") + " NOT NULL," +
                "user " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "level " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "x " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "y " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "z " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "type " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "data blob DEFAULT NULL," +
                "amount " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "action " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "FOREIGN KEY(user) REFERENCES users(id)," +
                "FOREIGN KEY(level) REFERENCES levels(id)," +
                "FOREIGN KEY(type) REFERENCES materials(id)" +
                ")";
        if (database.getDialect() instanceof MySQLDialect) {
            sql += " ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4;";
        } else {
            sql += ";";
        }
        database.createTable(sql);
    }

    public void createIndexes() {
        String sql;
        if (database.getDialect() instanceof MySQLDialect) {
            sql = "ALTER TABLE items ADD INDEX coordinates (x, y, z);";
        } else {
            sql = "CREATE INDEX IF NOT EXISTS coordinates ON items (x, y, z);";
        }
        database.execute(sql, false);
    }

    public void insert(long time, String userUuid, Level level, int x, int y, int z, SimpleItemStack item, int action) {
        if (item.isEmpty()) {
            return;
        }
        String materialQuery = database.getDialect().getInsertIgnore() + " INTO materials(name) VALUES(?);";

        String itemQuery = "INSERT INTO items(time, user, level, x, y, z, type, data, amount, action) " +
                "VALUES(?, (" +
                "SELECT id FROM users WHERE uuid = ?" +
                "), (" +
                "SELECT id FROM levels WHERE name = ?" +
                "), ?, ?, ?, (" +
                "SELECT id FROM materials WHERE name = ?" +
                "), ?, ?, ?);";

        ResourceLocation itemLocation = item.getItem().arch$registryName();
        if (itemLocation != null) {
            database.queue.add(connection -> {
                try (PreparedStatement materialStatement = connection.prepareStatement(materialQuery)) {
                    materialStatement.setString(1, itemLocation.toString().replace("minecraft:", ""));
                    materialStatement.executeUpdate();
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(itemQuery)) {
                    preparedStatement.setLong(1, time);
                    preparedStatement.setString(2, userUuid);
                    preparedStatement.setString(3, level.dimension().location().toString());
                    preparedStatement.setInt(4, x);
                    preparedStatement.setInt(5, y);
                    preparedStatement.setInt(6, z);
                    preparedStatement.setString(7, itemLocation.toString().replace("minecraft:", ""));
                    preparedStatement.setBytes(8, item.getTagBytes());
                    preparedStatement.setInt(9, item.getCount());
                    preparedStatement.setInt(10, action);
                    preparedStatement.executeUpdate();
                }
            });
        }
    }

    public void insertMap(long time, String userUuid, Level level, int x, int y, int z, Map<ItemAction, List<SimpleItemStack>> itemsMap) {
        String insertMaterialQuery = database.getDialect().getInsertIgnore() + " INTO materials(name) VALUES(?);";

        String insertItemQuery = "INSERT INTO items(time, user, level, x, y, z, type, data, amount, action) " +
                "VALUES(?, (" +
                "SELECT id FROM users WHERE uuid = ?" +
                "), (" +
                "SELECT id FROM levels WHERE name = ?" +
                "), ?, ?, ?, (" +
                "SELECT id FROM materials WHERE name = ?" +
                "), ?, ?, ?);";

        database.batchQueue.add(connection -> {
            try (PreparedStatement itemStatement = connection.prepareStatement(insertItemQuery);
                 PreparedStatement materialStatement = connection.prepareStatement(insertMaterialQuery)) {

            for (Map.Entry<ItemAction, List<SimpleItemStack>> entry : itemsMap.entrySet()) {
                for (SimpleItemStack item : entry.getValue()) {
                    if (item.isEmpty()) {
                        continue;
                    }
                    ResourceLocation itemLocation = item.getItem().arch$registryName();
                    if (itemLocation != null) {
                        materialStatement.setString(1, itemLocation.toString().replace("minecraft:", ""));
                        materialStatement.addBatch();

                            itemStatement.setLong(1, time);
                            itemStatement.setString(2, userUuid);
                            itemStatement.setString(3, level.dimension().location().toString());
                            itemStatement.setInt(4, x);
                            itemStatement.setInt(5, y);
                            itemStatement.setInt(6, z);
                            itemStatement.setString(7, itemLocation.toString().replace("minecraft:", ""));
                            itemStatement.setBytes(8, item.getTagBytes());
                            itemStatement.setInt(9, item.getCount());
                            itemStatement.setInt(10, entry.getKey().getId());
                            itemStatement.addBatch();
                        }
                    }
                }
                materialStatement.executeBatch();
                itemStatement.executeBatch();
            }
        });
    }

    public List<ItemHistory> getFilteredItemHistory(Level level, FilterList filterList) {
        @Nullable String actions = filterList.getActionString();
        @Nullable String users = filterList.getUserString();
        @Nullable String includeMaterials = filterList.getIncludeMaterialsString();
        @Nullable String excludeMaterials = filterList.getExcludeMaterialsString();

        String query = """
                SELECT items.time, users.name, users.uuid, items.x, items.y, items.z, materials.name, items.data, items.amount, items.action
                FROM items
                INNER JOIN users ON items.user = users.id
                INNER JOIN levels ON items.level = levels.id
                INNER JOIN materials ON items.type = materials.id
                WHERE levels.name = ?
                AND items.time > ?
                AND (? IS NULL OR items.action IN (%s))
                AND (? IS NULL OR users.id IN (%s))
                AND (? IS NULL OR materials.name IN ('%s'))
                AND (? IS NULL OR materials.name NOT IN ('%s'))
                AND (? IS NULL OR items.x BETWEEN ? AND ?)
                AND (? IS NULL OR items.y BETWEEN ? AND ?)
                AND (? IS NULL OR items.z BETWEEN ? AND ?)
                ORDER BY items.time DESC
                LIMIT 1000;
                """.formatted(actions, users, includeMaterials, excludeMaterials);

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, level.dimension().location().toString());
            preparedStatement.setLong(2, filterList.getTime());

            if (actions == null || actions.isEmpty()) {
                preparedStatement.setNull(3, Types.VARCHAR);
            } else {
                preparedStatement.setString(3, "not null");
            }

            if (users == null || users.isEmpty()) {
                preparedStatement.setNull(4, Types.VARCHAR);
            } else {
                preparedStatement.setString(4, "not null");
            }

            if (includeMaterials == null || includeMaterials.isEmpty()) {
                preparedStatement.setNull(5, Types.VARCHAR);
            } else {
                preparedStatement.setString(5, "not null");
            }

            if (excludeMaterials == null || excludeMaterials.isEmpty()) {
                preparedStatement.setNull(6, Types.VARCHAR);
            } else {
                preparedStatement.setString(6, "not null");
            }

            if (filterList.getRadiusFilter().isEmpty()) {
                preparedStatement.setNull(7, Types.VARCHAR);
            } else {
                preparedStatement.setString(7, "not null");
            }

            preparedStatement.setInt(8, filterList.getRadiusMinX());
            preparedStatement.setInt(9, filterList.getRadiusMaxX());

            if (filterList.getRadiusFilter().isEmpty()) {
                preparedStatement.setNull(10, Types.VARCHAR);
            } else {
                preparedStatement.setString(10, "not null");
            }

            preparedStatement.setInt(11, filterList.getRadiusMinY());
            preparedStatement.setInt(12, filterList.getRadiusMaxY());

            if (filterList.getRadiusFilter().isEmpty()) {
                preparedStatement.setNull(13, Types.VARCHAR);
            } else {
                preparedStatement.setString(13, "not null");
            }

            preparedStatement.setInt(14, filterList.getRadiusMinZ());
            preparedStatement.setInt(15, filterList.getRadiusMaxZ());

            List<ItemHistory> itemHistory = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                itemHistory.add(new ItemHistory(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getInt(4),
                        resultSet.getInt(5),
                        resultSet.getInt(6),
                        resultSet.getString(7),
                        resultSet.getBytes(8),
                        resultSet.getInt(9),
                        resultSet.getInt(10)));
            }
            return itemHistory;
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to get block history from database", exception);
            return List.of();
        }
    }
}