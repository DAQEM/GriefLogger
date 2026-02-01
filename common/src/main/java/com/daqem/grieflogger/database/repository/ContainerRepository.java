package com.daqem.grieflogger.database.repository;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.daqem.grieflogger.database.dialect.MySQLDialect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.Nullable;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.model.history.ContainerHistory;
import com.daqem.grieflogger.model.history.IHistory;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class ContainerRepository extends Repository {

    private final Database database;

    public ContainerRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS containers (" +
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
            sql = "ALTER TABLE containers ADD INDEX coordinates (x, y, z);";
        } else {
            sql = "CREATE INDEX IF NOT EXISTS coordinates ON containers (x, y, z);";
        }
        database.execute(sql, false);
    }

    public void insert(long time, String userUuid, Level level, int x, int y, int z, SimpleItemStack item, int itemAction) {
        if (item.isEmpty()) {
            return;
        }

        String insertMaterialQuery = database.getDialect().getInsertIgnore() + " INTO materials(name) VALUES(?);";

        String insertItemQuery = "INSERT INTO containers(time, user, level, x, y, z, type, data, amount, action) " +
                "VALUES(?, (" +
                "SELECT id FROM users WHERE uuid = ?" +
                "), (" +
                "SELECT id FROM levels WHERE name = ?" +
                "), ?, ?, ?, (" +
                "SELECT id FROM materials WHERE name = ?" +
                "), ?, ?, ?);";

        Identifier itemLocation = item.getItem().arch$registryName();
        if (itemLocation != null) {
            database.queue.add(connection -> {
                try (PreparedStatement materialStatement = connection.prepareStatement(insertMaterialQuery)) {
                    materialStatement.setString(1, itemLocation.toString().replace("minecraft:", ""));
                    materialStatement.executeUpdate();
                }
                try (PreparedStatement itemStatement = connection.prepareStatement(insertItemQuery)) {
                    itemStatement.setLong(1, time);
                    itemStatement.setString(2, userUuid);
                    itemStatement.setString(3, level.dimension().identifier().toString());
                    itemStatement.setInt(4, x);
                    itemStatement.setInt(5, y);
                    itemStatement.setInt(6, z);
                    itemStatement.setString(7, itemLocation.toString().replace("minecraft:", ""));
                    itemStatement.setBytes(8, item.getTagBytes(level));
                    itemStatement.setInt(9, item.getCount());
                    itemStatement.setInt(10, itemAction);
                    itemStatement.executeUpdate();
                }
            });
        }
    }

    public void insertList(long time, String userUuid, Level level, int x, int y, int z, List<SimpleItemStack> items, int itemAction) {
        String insertMaterialQuery = database.getDialect().getInsertIgnore() + " INTO materials(name) VALUES(?);";

        String insertItemQuery = "INSERT INTO containers(time, user, level, x, y, z, type, data, amount, action) " +
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

                for (SimpleItemStack item : items) {
                    if (item.isEmpty()) {
                        continue;
                    }
                    Identifier itemLocation = item.getItem().arch$registryName();
                    if (itemLocation != null) {
                        materialStatement.setString(1, itemLocation.toString().replace("minecraft:", ""));
                        materialStatement.addBatch();

                        itemStatement.setLong(1, time);
                        itemStatement.setString(2, userUuid);
                        itemStatement.setString(3, level.dimension().identifier().toString());
                        itemStatement.setInt(4, x);
                        itemStatement.setInt(5, y);
                        itemStatement.setInt(6, z);
                        itemStatement.setString(7, itemLocation.toString().replace("minecraft:", ""));
                        itemStatement.setBytes(8, item.getTagBytes(level));
                        itemStatement.setInt(9, item.getCount());
                        itemStatement.setInt(10, itemAction);
                        itemStatement.addBatch();
                    }
                }
                materialStatement.executeBatch();
                itemStatement.executeBatch();
            }
        });
    }

    public void insertMap(long time, String userUuid, Level level, int x, int y, int z, Map<ItemAction, List<SimpleItemStack>> itemsMap) {
        String insertMaterialQuery = database.getDialect().getInsertIgnore() + " INTO materials(name) VALUES(?);";

        String insertItemQuery = "INSERT INTO containers(time, user, level, x, y, z, type, data, amount, action) " +
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
                        Identifier itemLocation = item.getItem().arch$registryName();
                        if (itemLocation != null) {
                            materialStatement.setString(1, itemLocation.toString().replace("minecraft:", ""));
                            materialStatement.addBatch();

                            itemStatement.setLong(1, time);
                            itemStatement.setString(2, userUuid);
                            itemStatement.setString(3, level.dimension().identifier().toString());
                            itemStatement.setInt(4, x);
                            itemStatement.setInt(5, y);
                            itemStatement.setInt(6, z);
                            itemStatement.setString(7, itemLocation.toString().replace("minecraft:", ""));
                            itemStatement.setBytes(8, item.getTagBytes(level));
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

    private DataComponentPatch readPatch(byte[] bytes, Level level) {
        if (bytes == null || bytes.length == 0) {
            return DataComponentPatch.EMPTY;
        }
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            CompoundTag compoundTag = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
            return DataComponentPatch.CODEC.parse(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), compoundTag)
                    .getOrThrow(IllegalStateException::new);
        } catch (Exception e) {
            return DataComponentPatch.EMPTY;
        }
    }

    public List<IHistory> getHistory(Level level, int x, int y, int z) {
        List<IHistory> containerHistory = new ArrayList<>();
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
            preparedStatement.setString(1, level.dimension().identifier().toString());
            preparedStatement.setInt(2, x);
            preparedStatement.setInt(3, y);
            preparedStatement.setInt(4, z);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                DataComponentPatch patch = readPatch(resultSet.getBytes(8), level);
                containerHistory.add(new ContainerHistory(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getInt(4),
                        resultSet.getInt(5),
                        resultSet.getInt(6),
                        resultSet.getString(7),
                        patch,
                        resultSet.getInt(9),
                        resultSet.getInt(10)
                ));
            }
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to get container history", e);
        }
        return containerHistory;
    }

    public List<IHistory> getHistory(Level level, int x, int y, int z, int x2, int y2, int z2) {
        List<IHistory> containerHistory = new ArrayList<>();
        String query = """
                SELECT containers.time, users.name, users.uuid, containers.x, containers.y, containers.z, materials.name, containers.data, containers.amount, containers.action
                FROM containers
                INNER JOIN users ON containers.user = users.id
                INNER JOIN levels ON containers.level = (
                    SELECT id FROM levels WHERE name = ?
                )
                INNER JOIN materials ON containers.type = materials.id
                WHERE containers.level = levels.id AND containers.x BETWEEN ? AND ? AND containers.y BETWEEN ? AND ? AND containers.z BETWEEN ? AND ? AND (containers.action = 0 OR containers.action = 1)
                ORDER BY containers.time DESC
                """;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, level.dimension().identifier().toString());
            preparedStatement.setInt(2, x);
            preparedStatement.setInt(3, x2);
            preparedStatement.setInt(4, y);
            preparedStatement.setInt(5, y2);
            preparedStatement.setInt(6, z);
            preparedStatement.setInt(7, z2);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                DataComponentPatch patch = readPatch(resultSet.getBytes(8), level);
                containerHistory.add(new ContainerHistory(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getInt(4),
                        resultSet.getInt(5),
                        resultSet.getInt(6),
                        resultSet.getString(7),
                        patch,
                        resultSet.getInt(9),
                        resultSet.getInt(10)
                ));
            }
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to get container history", e);
        }
        return containerHistory;
    }

    public List<IHistory> getFilteredContainerHistory(Level level, FilterList filterList) {
        @Nullable String actions = filterList.getActionString();
        @Nullable String users = filterList.getUserString();
        @Nullable String includeMaterials = filterList.getIncludeMaterialsString();
        @Nullable String excludeMaterials = filterList.getExcludeMaterialsString();

        String query = """
                SELECT containers.time, users.name, users.uuid, containers.x, containers.y, containers.z, materials.name, containers.data, containers.amount, containers.action
                FROM containers
                INNER JOIN users ON containers.user = users.id
                INNER JOIN levels ON containers.level = levels.id
                INNER JOIN materials ON containers.type = materials.id
                WHERE levels.name = ?
                AND containers.time > ?
                AND (? IS NULL OR containers.action IN (%s))
                AND (? IS NULL OR users.id IN (%s))
                AND (? IS NULL OR materials.name IN ('%s'))
                AND (? IS NULL OR materials.name NOT IN ('%s'))
                AND (? IS NULL OR containers.x BETWEEN ? AND ?)
                AND (? IS NULL OR containers.y BETWEEN ? AND ?)
                AND (? IS NULL OR containers.z BETWEEN ? AND ?)
                ORDER BY containers.time DESC
                LIMIT 1000;
                """.formatted(actions, users, includeMaterials, excludeMaterials);

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, level.dimension().identifier().toString());
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

            List<IHistory> blockHistory = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                DataComponentPatch patch = readPatch(resultSet.getBytes(8), level);
                blockHistory.add(new ContainerHistory(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getInt(4),
                        resultSet.getInt(5),
                        resultSet.getInt(6),
                        resultSet.getString(7),
                        patch,
                        resultSet.getInt(9),
                        resultSet.getInt(10)));
            }
            return blockHistory;
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to get block history from database", exception);
            return List.of();
        }
    }
}