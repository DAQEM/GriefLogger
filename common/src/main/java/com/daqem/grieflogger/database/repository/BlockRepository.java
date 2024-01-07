package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.model.*;

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
        database.execute("""
                CREATE TABLE IF NOT EXISTS blocks (
                	time integer NOT NULL,
                	user integer NOT NULL,
                	level integer NOT NULL,
                	x integer NOT NULL,
                	y integer NOT NULL,
                	z integer NOT NULL,
                	type integer NOT NULL,
                	blockAction integer NOT NULL
                );
                """);
    }

    public void insertMaterial(long time, String userUuid, String levelName, int x, int y, int z, String material, int blockAction) {
        database.executeUpdate("""
                INSERT OR IGNORE INTO materials(name)
                VALUES('%s');
                
                INSERT OR IGNORE INTO blocks(time, user, level, x, y, z, type, blockAction)
                VALUES(%d, (
                    SELECT id FROM users WHERE uuid = '%s'
                ), (
                    SELECT id FROM levels WHERE name = '%s'
                ), %d, %d, %d, (
                    SELECT id FROM materials WHERE name = '%s'
                ), %d);
                """.formatted(material, time, userUuid, levelName, x, y, z, material, blockAction));
    }

    public void insertEntity(long time, String userUuid, String levelName, int x, int y, int z, String entity, int blockAction) {
        database.executeUpdate("""
                INSERT OR IGNORE INTO entities(name)
                VALUES('%s');
                
                INSERT OR IGNORE INTO blocks(time, user, level, x, y, z, type, blockAction)
                VALUES(%d, (
                    SELECT id FROM users WHERE uuid = '%s'
                ), (
                    SELECT id FROM levels WHERE name = '%s'
                ), %d, %d, %d, (
                    SELECT id FROM entities WHERE name = '%s'
                ), %d);
                """.formatted(entity, time, userUuid, levelName, x, y, z, entity, blockAction));
    }

    public List<BlockHistory> getHistory(String levelName, int x, int y, int z) {
        List<BlockHistory> blockHistory;
        try (ResultSet resultSet = database.executeQuery("""
                SELECT blocks.time, users.name, users.uuid, levels.name, blocks.x, blocks.y, blocks.z, materials.name, blocks.blockAction
                FROM blocks
                INNER JOIN users ON blocks.user = users.id
                INNER JOIN levels ON blocks.level = (
                    SELECT id FROM levels WHERE name = '%s'
                )
                INNER JOIN materials ON blocks.type = materials.id
                WHERE blocks.level = levels.id AND blocks.x = %d AND blocks.y = %d AND blocks.z = %d AND (blocks.blockAction = 0 OR blocks.blockAction = 1 OR blocks.blockAction = 2)
                ORDER BY blocks.time DESC
                """.formatted(levelName, x, y, z))) {
            blockHistory = new ArrayList<>();
            while (resultSet.next()) {
                blockHistory.add(new BlockHistory(
                        new Time(resultSet.getLong(1)),
                        new User(
                                resultSet.getString(2),
                                resultSet.getString(3) == null ? null : java.util.UUID.fromString(resultSet.getString(3))
                        ),
                        resultSet.getString(4),
                        new BlockPosition(resultSet.getInt(5), resultSet.getInt(6), resultSet.getInt(7)),
                        resultSet.getString(8),
                        BlockAction.fromId(resultSet.getInt(9))
                ));
            }
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to get block history", e);
            return null;
        }
        return blockHistory;
    }

    public void removeInteractionsForPosition(String levelName, int x, int y, int z) {
        database.executeUpdate("""
                DELETE FROM blocks
                WHERE level = (
                    SELECT id FROM levels WHERE name = '%s'
                ) AND x = %d AND y = %d AND z = %d AND blockAction = 2
                """.formatted(levelName, x, y, z));
    }
}
