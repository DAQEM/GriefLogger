package com.daqem.grieflogger.database.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.daqem.grieflogger.database.dialect.MySQLDialect;
import org.jetbrains.annotations.Nullable;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.model.history.BlockHistory;
import com.daqem.grieflogger.model.history.IHistory;

public class BlockRepository extends Repository {

    private final Database database;

    public BlockRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS blocks (" +
                "time " + database.getDialect().getDataType("bigint") + " NOT NULL," +
                "user " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "level " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "x " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "y " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "z " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "type " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "action " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "FOREIGN KEY(user) REFERENCES users(id)," +
                "FOREIGN KEY(level) REFERENCES levels(id)" +
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
            sql = "ALTER TABLE blocks ADD INDEX coordinates (x, y, z);";
        } else {
            sql = "CREATE INDEX IF NOT EXISTS coordinates ON blocks (x, y, z);";
        }
        database.execute(sql, false);
    }

    public void insertMaterial(long time, String userUuid, String levelName, int x, int y, int z, String material, int blockAction) {
        String materialQuery = database.getDialect().getInsertIgnore() + " INTO materials(name) VALUES(?);";

        String blockQuery = database.getDialect().getInsertIgnore() + " INTO blocks(time, user, level, x, y, z, type, action) " +
                "VALUES(?, (" +
                "SELECT id FROM users WHERE uuid = ?" +
                "), (" +
                "SELECT id FROM levels WHERE name = ?" +
                "), ?, ?, ?, (" +
                "SELECT id FROM materials WHERE name = ?" +
                "), ?);";

        database.queue.add(connection -> {
            try (PreparedStatement materialStatement = connection.prepareStatement(materialQuery)) {
                materialStatement.setString(1, material);
                materialStatement.executeUpdate();
            }
            try (PreparedStatement blockStatement = connection.prepareStatement(blockQuery)) {
                blockStatement.setLong(1, time);
                blockStatement.setString(2, userUuid);
                blockStatement.setString(3, levelName);
                blockStatement.setInt(4, x);
                blockStatement.setInt(5, y);
                blockStatement.setInt(6, z);
                blockStatement.setString(7, material);
                blockStatement.setInt(8, blockAction);
                blockStatement.executeUpdate();
            }
        });
    }

    public void insertEntity(long time, String userUuid, String levelName, int x, int y, int z, String entity, int blockAction) {
        String materialQuery = database.getDialect().getInsertIgnore() + " INTO entities(name) VALUES(?);";

        String blockQuery = database.getDialect().getInsertIgnore() + " INTO blocks(time, user, level, x, y, z, type, action) " +
                "VALUES(?, (" +
                "SELECT id FROM users WHERE uuid = ?" +
                "), (" +
                "SELECT id FROM levels WHERE name = ?" +
                "), ?, ?, ?, (" +
                "SELECT id FROM entities WHERE name = ?" +
                "), ?);";

        database.queue.add(connection -> {
            try (PreparedStatement materialStatement = connection.prepareStatement(materialQuery)) {
                materialStatement.setString(1, entity);
                materialStatement.executeUpdate();
            }
            try (PreparedStatement blockStatement = connection.prepareStatement(blockQuery)) {
                blockStatement.setLong(1, time);
                blockStatement.setString(2, userUuid);
                blockStatement.setString(3, levelName);
                blockStatement.setInt(4, x);
                blockStatement.setInt(5, y);
                blockStatement.setInt(6, z);
                blockStatement.setString(7, entity);
                blockStatement.setInt(8, blockAction);
                blockStatement.executeUpdate();
            }
        });
    }

    public List<IHistory> getBlockHistory(String levelName, int x, int y, int z) {
        List<IHistory> blockHistory = new ArrayList<>();
        String query = """
                SELECT blocks.time, users.name, users.uuid, blocks.x, blocks.y, blocks.z, materials.name, blocks.action
                FROM blocks
                INNER JOIN users ON blocks.user = users.id
                INNER JOIN levels ON blocks.level = (
                    SELECT id FROM levels WHERE name = ?
                )
                INNER JOIN materials ON blocks.type = materials.id
                WHERE blocks.level = levels.id AND blocks.x = ? AND blocks.y = ? AND blocks.z = ? AND (blocks.action = 0 OR blocks.action = 1)
                ORDER BY blocks.time DESC
                """;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, levelName);
            preparedStatement.setInt(2, x);
            preparedStatement.setInt(3, y);
            preparedStatement.setInt(4, z);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                blockHistory.add(new BlockHistory(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getInt(4),
                        resultSet.getInt(5),
                        resultSet.getInt(6),
                        resultSet.getString(7),
                        resultSet.getInt(8)
                ));
            }
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to get block history", e);
        }
        return blockHistory;
    }

    public List<IHistory> getInteractionHistory(String levelName, int x, int y, int z) {
        List<IHistory> blockHistory = new ArrayList<>();
        String query = """
                SELECT blocks.time, users.name, users.uuid, blocks.x, blocks.y, blocks.z,
                CASE WHEN blocks.action = 4 THEN entities.name ELSE materials.name END,
                blocks.action
                FROM blocks
                INNER JOIN users ON blocks.user = users.id
                INNER JOIN levels ON blocks.level = (
                    SELECT id FROM levels WHERE name = ?
                )
                LEFT JOIN materials ON blocks.type = materials.id AND blocks.action = 2
                LEFT JOIN entities ON blocks.type = entities.id AND blocks.action = 4
                WHERE blocks.level = levels.id AND blocks.x = ? AND blocks.y = ? AND blocks.z = ? AND (blocks.action = 2 OR blocks.action = 4)
                ORDER BY blocks.time DESC
                """;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, levelName);
            preparedStatement.setInt(2, x);
            preparedStatement.setInt(3, y);
            preparedStatement.setInt(4, z);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                blockHistory.add(new BlockHistory(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getInt(4),
                        resultSet.getInt(5),
                        resultSet.getInt(6),
                        resultSet.getString(7),
                        resultSet.getInt(8)
                ));
            }
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to get block history", e);
        }
        return blockHistory;
    }

    public void removeInteractionsForPosition(String levelName, int x, int y, int z) {
        String query = """
                DELETE FROM blocks
                WHERE level = (
                    SELECT id FROM levels WHERE name = ?
                ) AND x = ? AND y = ? AND z = ? AND action = 2
                """;

        database.queue.add(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, levelName);
                preparedStatement.setInt(2, x);
                preparedStatement.setInt(3, y);
                preparedStatement.setInt(4, z);
                preparedStatement.executeUpdate();
            }
        });
    }

    public List<IHistory> getFilteredBlockHistory(String levelName, FilterList filterList) {
        @Nullable String actions = filterList.getActionString();
        @Nullable String users = filterList.getUserString();
        @Nullable String includeMaterials = filterList.getIncludeMaterialsString();
        @Nullable String excludeMaterials = filterList.getExcludeMaterialsString();

        String query = """
                SELECT
                    blocks.time,
                    users.name,
                    users.uuid,
                    blocks.x,
                    blocks.y,
                    blocks.z,
                    CASE
                        WHEN blocks.action = 3 OR blocks.action = 4 THEN entities.name
                        ELSE materials.name
                    END AS type_name,
                    blocks.action
                FROM
                    blocks
                INNER JOIN users ON blocks.user = users.id
                INNER JOIN levels ON blocks.level = levels.id
                LEFT JOIN materials ON blocks.type = materials.id AND blocks.action != 3 AND blocks.action != 4
                LEFT JOIN entities ON blocks.type = entities.id AND (blocks.action = 3 OR blocks.action = 4)
                WHERE
                    levels.name = ?
                    AND blocks.time > ?
                    AND (? IS NULL OR blocks.action IN (%s))
                    AND (? IS NULL OR users.id IN (%s))
                    AND (? IS NULL OR materials.name IN ('%s'))
                    AND (? IS NULL OR materials.name NOT IN ('%s'))
                    AND (? IS NULL OR blocks.x BETWEEN ? AND ?)
                    AND (? IS NULL OR blocks.y BETWEEN ? AND ?)
                    AND (? IS NULL OR blocks.z BETWEEN ? AND ?)
                ORDER BY
                    blocks.time DESC
                LIMIT 1000;
                """.formatted(actions, users, includeMaterials, excludeMaterials);

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, levelName);
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
                blockHistory.add(new BlockHistory(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getInt(4),
                        resultSet.getInt(5),
                        resultSet.getInt(6),
                        resultSet.getString(7),
                        resultSet.getInt(8)));
            }
            return blockHistory;
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to get block history from database", exception);
            return List.of();
        }
    }
}