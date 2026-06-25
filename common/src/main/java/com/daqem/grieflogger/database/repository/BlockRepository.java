package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.model.history.BlockHistory;
import com.daqem.grieflogger.model.history.IHistory;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BlockRepository extends Repository {

    private final Database database;

    public BlockRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS blocks (
                	time integer NOT NULL,
                	user integer NOT NULL,
                	level integer NOT NULL,
                	x integer NOT NULL,
                	y integer NOT NULL,
                	z integer NOT NULL,
                	type integer NOT NULL,
                	action integer NOT NULL,
                	FOREIGN KEY(user) REFERENCES users(id),
                	FOREIGN KEY(level) REFERENCES levels(id),
                	FOREIGN KEY(type) REFERENCES materials(id)
                );
                """;
        if (isMysql()) {
            sql = """
                    CREATE TABLE IF NOT EXISTS blocks (
                    	time bigint NOT NULL,
                    	user int NOT NULL,
                    	level int NOT NULL,
                    	x int NOT NULL,
                    	y int NOT NULL,
                    	z int NOT NULL,
                    	type int NOT NULL,
                    	action int NOT NULL,
                    	FOREIGN KEY(user) REFERENCES users(id),
                    	FOREIGN KEY(level) REFERENCES levels(id),
                    	FOREIGN KEY(type) REFERENCES materials(id)
                    )
                    ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4;
                    """;
        }
        database.createTable(sql);
    }

    public void createIndexes() {
        String sql = """
                CREATE INDEX IF NOT EXISTS coordinates ON blocks (x, y, z);
                """;
        if (isMysql()) {
            sql = """
                    ALTER TABLE blocks ADD INDEX coordinates (x, y, z);
                    """;
        }
        database.execute(sql, false);
    }

    public void insertMaterial(long time, String userUuid, String levelName, int x, int y, int z, String material, int blockAction) {
        boolean mysql = isMysql();
        String levelQuery = mysql ? BlockSql.LEVEL_UPSERT_MYSQL : BlockSql.LEVEL_UPSERT_SQLITE;
        String materialQuery = mysql ? BlockSql.MATERIAL_UPSERT_MYSQL : BlockSql.MATERIAL_UPSERT_SQLITE;
        String blockQuery = mysql ? BlockSql.BLOCK_INSERT_MYSQL : BlockSql.BLOCK_INSERT_SQLITE;

        try {
            // Ensure parent rows exist before the dependent insert so the FK sub-selects resolve;
            // otherwise a not-yet-persisted level/material yields NULL -> NOT NULL violation ->
            // the event is silently dropped by INSERT OR IGNORE. (GAP E)
            PreparedStatement levelStatement = database.prepareStatement(levelQuery);
            levelStatement.setString(1, levelName);
            database.queue.add(levelStatement);

            PreparedStatement materialStatement = database.prepareStatement(materialQuery);
            materialStatement.setString(1, material);
            database.queue.add(materialStatement);

            PreparedStatement blockStatement = database.prepareStatement(blockQuery);
            blockStatement.setLong(1, time);
            blockStatement.setString(2, userUuid);
            blockStatement.setString(3, levelName);
            blockStatement.setInt(4, x);
            blockStatement.setInt(5, y);
            blockStatement.setInt(6, z);
            blockStatement.setString(7, material);
            blockStatement.setInt(8, blockAction);
            database.queue.add(blockStatement);
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert block into database", exception);
        }
    }

    public void insertEntity(long time, String userUuid, String levelName, int x, int y, int z, String entity, int blockAction) {
        boolean mysql = isMysql();
        String levelQuery = mysql ? BlockSql.LEVEL_UPSERT_MYSQL : BlockSql.LEVEL_UPSERT_SQLITE;
        String entityQuery = mysql ? BlockSql.ENTITY_UPSERT_MYSQL : BlockSql.ENTITY_UPSERT_SQLITE;
        String blockQuery = mysql ? BlockSql.ENTITY_BLOCK_INSERT_MYSQL : BlockSql.ENTITY_BLOCK_INSERT_SQLITE;

        try {
            // Ensure parent rows exist before the dependent insert (see insertMaterial / GAP E).
            PreparedStatement levelStatement = database.prepareStatement(levelQuery);
            levelStatement.setString(1, levelName);
            database.queue.add(levelStatement);

            PreparedStatement entityStatement = database.prepareStatement(entityQuery);
            entityStatement.setString(1, entity);
            database.queue.add(entityStatement);

            PreparedStatement blockStatement = database.prepareStatement(blockQuery);
            blockStatement.setLong(1, time);
            blockStatement.setString(2, userUuid);
            blockStatement.setString(3, levelName);
            blockStatement.setInt(4, x);
            blockStatement.setInt(5, y);
            blockStatement.setInt(6, z);
            blockStatement.setString(7, entity);
            blockStatement.setInt(8, blockAction);
            database.queue.add(blockStatement);
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert block into database", exception);
        }
    }

    public List<IHistory> getBlockHistory(String levelName, int x, int y, int z) {
        List<IHistory> blockHistory = new ArrayList<>();
        String query = isMysql() ? BlockSql.BLOCK_HISTORY_BY_POSITION_MYSQL : BlockSql.BLOCK_HISTORY_BY_POSITION_SQLITE;

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
        String query = isMysql() ? BlockSql.INTERACTION_HISTORY_BY_POSITION_MYSQL : BlockSql.INTERACTION_HISTORY_BY_POSITION_SQLITE;

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

        try {
            PreparedStatement preparedStatement = database.prepareStatement(query);
            preparedStatement.setString(1, levelName);
            preparedStatement.setInt(2, x);
            preparedStatement.setInt(3, y);
            preparedStatement.setInt(4, z);
            database.queue.add(preparedStatement);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to remove interactions for position", e);
        }
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
                        WHEN blocks.action = 3 THEN entities.name
                        ELSE materials.name
                    END AS type_name,
                    blocks.action
                FROM
                    blocks
                INNER JOIN users ON blocks.user = users.id
                INNER JOIN levels ON blocks.level = levels.id
                LEFT JOIN materials ON blocks.type = materials.id AND blocks.action != 3
                LEFT JOIN entities ON blocks.type = entities.id AND blocks.action = 3
                WHERE
                    levels.name = ?
                    AND blocks.time > ?
                    AND (? IS NULL OR blocks.action IN (%s))
                    AND (? IS NULL OR users.id IN (%s))
                    AND (? IS NULL OR materials.name IN ('%s'))
                    AND (? IS NULL OR materials.name NOT IN ('%s'))
                    AND blocks.x BETWEEN ? AND ?
                    AND blocks.y BETWEEN ? AND ?
                    AND blocks.z BETWEEN ? AND ?
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

            preparedStatement.setInt(7, filterList.getRadiusMinX());
            preparedStatement.setInt(8, filterList.getRadiusMaxX());
            preparedStatement.setInt(9, filterList.getRadiusMinY());
            preparedStatement.setInt(10, filterList.getRadiusMaxY());
            preparedStatement.setInt(11, filterList.getRadiusMinZ());
            preparedStatement.setInt(12, filterList.getRadiusMaxZ());

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
