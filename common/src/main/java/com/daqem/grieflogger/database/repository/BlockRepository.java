package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.model.*;
import net.minecraft.core.BlockPos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                	material integer NOT NULL,
                	action integer NOT NULL
                );
                """);
    }

    public void insert(long time, int user, int level, int x, int y, int z, int material, int action) {
        database.executeUpdate("""
                INSERT INTO blocks(time, user, level, x, y, z, material, action)
                VALUES(%d, %d, %d, %d, %d, %d, %d, %d)
                """.formatted(time, user, level, x, y, z, material, action));
    }

    public List<BlockHistory> getHistory(String levelName, int x, int y, int z) {
        ResultSet resultSet = database.executeQuery("""
                SELECT blocks.time, users.name, users.uuid, levels.name, blocks.x, blocks.y, blocks.z, materials.material, blocks.action
                FROM blocks
                INNER JOIN users ON blocks.user = users.id
                INNER JOIN levels ON blocks.level = (
                    SELECT id FROM levels WHERE name = '%s'
                )
                INNER JOIN materials ON blocks.material = materials.id
                WHERE blocks.level = levels.id AND blocks.x = %d AND blocks.y = %d AND blocks.z = %d
                ORDER BY blocks.time DESC
                """.formatted(levelName, x, y, z));
        List<BlockHistory> blockHistory = new ArrayList<>();
        try {
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
                        Action.fromId(resultSet.getInt(9))
                ));
            }
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to get block history", e);
        }
        return blockHistory;
    }
}
