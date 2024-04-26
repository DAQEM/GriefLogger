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
        String materialQuery = """
                INSERT OR IGNORE INTO materials(name)
                VALUES(?);
                """;

        if (isMysql()) {
            materialQuery = """
                    INSERT IGNORE INTO materials(name)
                    VALUES(?);
                    """;
        }

        String blockQuery = """
                INSERT OR IGNORE INTO blocks(time, user, level, x, y, z, type, action)
                VALUES(?, (
                    SELECT id FROM users WHERE uuid = ?
                ), (
                    SELECT id FROM levels WHERE name = ?
                ), ?, ?, ?, (
                    SELECT id FROM materials WHERE name = ?
                ), ?);
                """;

        if (isMysql()) {
            blockQuery = """
                    INSERT IGNORE INTO blocks(time, user, level, x, y, z, type, action)
                    VALUES(?, (
                        SELECT id FROM users WHERE uuid = ?
                    ), (
                        SELECT id FROM levels WHERE name = ?
                    ), ?, ?, ?, (
                        SELECT id FROM materials WHERE name = ?
                    ), ?);
                    """;
        }

        try {
            PreparedStatement materialStatement = database.prepareStatement(materialQuery);
            PreparedStatement blockStatement = database.prepareStatement(blockQuery);
            materialStatement.setString(1, material);
            database.queue.add(materialStatement);

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
        String materialQuery = """
                INSERT OR IGNORE INTO entities(name)
                VALUES(?);
                """;

        if (isMysql()) {
            materialQuery = """
                    INSERT IGNORE INTO entities(name)
                    VALUES(?);
                    """;
        }

        String blockQuery = """
                INSERT OR IGNORE INTO blocks(time, user, level, x, y, z, type, action)
                VALUES(?, (
                    SELECT id FROM users WHERE uuid = ?
                ), (
                    SELECT id FROM levels WHERE name = ?
                ), ?, ?, ?, (
                    SELECT id FROM entities WHERE name = ?
                ), ?);
                """;

        if (isMysql()) {
            blockQuery = """
                    INSERT IGNORE INTO blocks(time, user, level, x, y, z, type, action)
                    VALUES(?, (
                        SELECT id FROM users WHERE uuid = ?
                    ), (
                        SELECT id FROM levels WHERE name = ?
                    ), ?, ?, ?, (
                        SELECT id FROM entities WHERE name = ?
                    ), ?);
                    """;
        }


        try {
            PreparedStatement materialStatement = database.prepareStatement(materialQuery);
            PreparedStatement blockStatement = database.prepareStatement(blockQuery);
            materialStatement.setString(1, entity);
            database.queue.add(materialStatement);

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
                SELECT blocks.time, users.name, users.uuid, blocks.x, blocks.y, blocks.z, materials.name, blocks.action
                FROM blocks
                INNER JOIN users ON blocks.user = users.id
                INNER JOIN levels ON blocks.level = (
                    SELECT id FROM levels WHERE name = ?
                )
                INNER JOIN materials ON blocks.type = materials.id
                WHERE blocks.level = levels.id AND blocks.x = ? AND blocks.y = ? AND blocks.z = ? AND blocks.action = 2
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
                SELECT blocks.time, users.name, users.uuid, blocks.x, blocks.y, blocks.z, materials.name, blocks.action
                FROM blocks
                INNER JOIN users ON blocks.user = users.id
                INNER JOIN levels ON blocks.level = levels.id
                INNER JOIN materials ON blocks.type = materials.id
                WHERE levels.name = ?
                AND blocks.time > ?
                AND (? IS NULL OR blocks.action IN (%s))
                AND (? IS NULL OR users.id IN (%s))
                AND (? IS NULL OR materials.name IN ('%s'))
                AND (? IS NULL OR materials.name NOT IN ('%s'))
                AND blocks.x BETWEEN ? AND ?
                AND blocks.y BETWEEN ? AND ?
                AND blocks.z BETWEEN ? AND ?
                ORDER BY blocks.time DESC
                LIMIT 1000;
                """.formatted(actions, users, includeMaterials, excludeMaterials);

        if (actions != null && !actions.isEmpty()
                && actions.contains(Integer.toString(BlockAction.KILL_ENTITY.getId()))) {
            query = query.replaceAll("materials", "entities");
        }

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
