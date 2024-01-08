package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.model.history.BlockHistory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BlockRepository {

    private final Database database;

    public BlockRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.createTable("""
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
                """);
    }

    public void insertMaterial(long time, String userUuid, String levelName, int x, int y, int z, String material, int blockAction) {
        String materialQuery = """
                INSERT OR IGNORE INTO materials(name)
                VALUES(?);
                """;

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

        try (PreparedStatement materialStatement = database.prepareStatement(materialQuery);
             PreparedStatement blockStatement = database.prepareStatement(blockQuery)) {
            materialStatement.setString(1, material);
            materialStatement.executeUpdate();

            blockStatement.setLong(1, time);
            blockStatement.setString(2, userUuid);
            blockStatement.setString(3, levelName);
            blockStatement.setInt(4, x);
            blockStatement.setInt(5, y);
            blockStatement.setInt(6, z);
            blockStatement.setString(7, material);
            blockStatement.setInt(8, blockAction);
            blockStatement.executeUpdate();
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert block into database", exception);
        }
    }

    public void insertEntity(long time, String userUuid, String levelName, int x, int y, int z, String entity, int blockAction) {
        String materialQuery = """
                INSERT OR IGNORE INTO entities(name)
                VALUES(?);
                """;

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

        try (PreparedStatement materialStatement = database.prepareStatement(materialQuery);
             PreparedStatement blockStatement = database.prepareStatement(blockQuery)) {
            materialStatement.setString(1, entity);
            materialStatement.executeUpdate();

            blockStatement.setLong(1, time);
            blockStatement.setString(2, userUuid);
            blockStatement.setString(3, levelName);
            blockStatement.setInt(4, x);
            blockStatement.setInt(5, y);
            blockStatement.setInt(6, z);
            blockStatement.setString(7, entity);
            blockStatement.setInt(8, blockAction);
            blockStatement.executeUpdate();
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert block into database", exception);
        }
    }

    public List<BlockHistory> getBlockHistory(String levelName, int x, int y, int z) {
        List<BlockHistory> blockHistory = new ArrayList<>();
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

    public List<BlockHistory> getInteractionHistory(String levelName, int x, int y, int z) {
        List<BlockHistory> blockHistory = new ArrayList<>();
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

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, levelName);
            preparedStatement.setInt(2, x);
            preparedStatement.setInt(3, y);
            preparedStatement.setInt(4, z);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to remove interactions for position", e);
        }
    }
}
