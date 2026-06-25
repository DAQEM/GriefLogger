package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.model.action.ItemAction;
import com.daqem.grieflogger.model.history.ContainerHistory;
import com.daqem.grieflogger.model.history.IHistory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

public class ContainerRepository extends Repository {

    private final Database database;

    public ContainerRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = """
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
                );
                """;
        if (isMysql()) {
            sql = """
                    CREATE TABLE IF NOT EXISTS containers (
                        time bigint NOT NULL,
                    	user int NOT NULL,
                    	level int NOT NULL,
                    	x int NOT NULL,
                    	y int NOT NULL,
                    	z int NOT NULL,
                    	type int NOT NULL,
                    	data blob DEFAULT NULL,
                    	amount int NOT NULL,
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
                CREATE INDEX IF NOT EXISTS coordinates ON containers (x, y, z);
                """;
        if (isMysql()) {
            sql = """
                    ALTER TABLE containers ADD INDEX coordinates (x, y, z);
                    """;
        }
        database.execute(sql, false);
    }

    public void insert(long time, String userUuid, Level level, int x, int y, int z, SimpleItemStack item, int itemAction) {
        if (item.isEmpty()) {
            return;
        }

        boolean mysql = isMysql();
        String levelQuery = mysql ? ContainerSql.LEVEL_UPSERT_MYSQL : ContainerSql.LEVEL_UPSERT_SQLITE;
        String materialQuery = mysql ? ContainerSql.MATERIAL_UPSERT_MYSQL : ContainerSql.MATERIAL_UPSERT_SQLITE;
        String insertItemQuery = mysql ? ContainerSql.CONTAINER_INSERT_MYSQL : ContainerSql.CONTAINER_INSERT_SQLITE;

        ResourceLocation itemLocation = item.getItem().arch$registryName();
        if (itemLocation != null) {
            try {
                String materialName = itemLocation.toString().replace("minecraft:", "");
                String levelName = level.dimension().location().toString();

                // Ensure parent rows exist before the dependent insert so the FK sub-selects
                // resolve; otherwise a not-yet-persisted level/material yields NULL -> NOT NULL
                // violation -> the plain INSERT throws and aborts the whole flush batch. (GAP E)
                PreparedStatement levelStatement = database.prepareStatement(levelQuery);
                levelStatement.setString(1, levelName);
                database.queue.add(levelStatement);

                PreparedStatement materialStatement = database.prepareStatement(materialQuery);
                materialStatement.setString(1, materialName);
                database.queue.add(materialStatement);

                PreparedStatement itemStatement = database.prepareStatement(insertItemQuery);
                itemStatement.setLong(1, time);
                itemStatement.setString(2, userUuid);
                itemStatement.setString(3, levelName);
                itemStatement.setInt(4, x);
                itemStatement.setInt(5, y);
                itemStatement.setInt(6, z);
                itemStatement.setString(7, materialName);
                itemStatement.setBytes(8, item.getTagBytes(level));
                itemStatement.setInt(9, item.getCount());
                itemStatement.setInt(10, itemAction);
                database.queue.add(itemStatement);
            } catch (SQLException e) {
                GriefLogger.LOGGER.error("Failed to insert item", e);
            }
        }
    }

    public void insertList(long time, String userUuid, Level level, int x, int y, int z, List<SimpleItemStack> items, int itemAction) {
        boolean mysql = isMysql();
        String levelQuery = mysql ? ContainerSql.LEVEL_UPSERT_MYSQL : ContainerSql.LEVEL_UPSERT_SQLITE;
        String insertMaterialQuery = mysql ? ContainerSql.MATERIAL_UPSERT_MYSQL : ContainerSql.MATERIAL_UPSERT_SQLITE;
        String insertItemQuery = mysql ? ContainerSql.CONTAINER_INSERT_MYSQL : ContainerSql.CONTAINER_INSERT_SQLITE;

        try {
            String levelName = level.dimension().location().toString();

            // Upsert the (single) level parent in the same batch, ahead of the inserts. (GAP E)
            PreparedStatement levelStatement = database.prepareStatement(levelQuery);
            levelStatement.setString(1, levelName);
            levelStatement.addBatch();

            PreparedStatement itemStatement = database.prepareStatement(insertItemQuery);
            PreparedStatement materialStatement = database.prepareStatement(insertMaterialQuery);

            for (SimpleItemStack item : items) {
                if (item.isEmpty()) {
                    continue;
                }
                ResourceLocation itemLocation = item.getItem().arch$registryName();
                if (itemLocation != null) {
                    String materialName = itemLocation.toString().replace("minecraft:", "");
                    materialStatement.setString(1, materialName);
                    materialStatement.addBatch();

                    itemStatement.setLong(1, time);
                    itemStatement.setString(2, userUuid);
                    itemStatement.setString(3, levelName);
                    itemStatement.setInt(4, x);
                    itemStatement.setInt(5, y);
                    itemStatement.setInt(6, z);
                    itemStatement.setString(7, materialName);
                    itemStatement.setBytes(8, item.getTagBytes(level));
                    itemStatement.setInt(9, item.getCount());
                    itemStatement.setInt(10, itemAction);
                    itemStatement.addBatch();
                }
            }
            database.batchQueue.add(levelStatement);
            database.batchQueue.add(materialStatement);
            database.batchQueue.add(itemStatement);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to insert item", e);
        }
    }

    public void insertMap(long time, String userUuid, Level level, int x, int y, int z, Map<ItemAction, List<SimpleItemStack>> itemsMap) {
        boolean mysql = isMysql();
        String levelQuery = mysql ? ContainerSql.LEVEL_UPSERT_MYSQL : ContainerSql.LEVEL_UPSERT_SQLITE;
        String insertMaterialQuery = mysql ? ContainerSql.MATERIAL_UPSERT_MYSQL : ContainerSql.MATERIAL_UPSERT_SQLITE;
        String insertItemQuery = mysql ? ContainerSql.CONTAINER_INSERT_MYSQL : ContainerSql.CONTAINER_INSERT_SQLITE;

        try {
            String levelName = level.dimension().location().toString();

            // Upsert the (single) level parent in the same batch, ahead of the inserts. (GAP E)
            PreparedStatement levelStatement = database.prepareStatement(levelQuery);
            levelStatement.setString(1, levelName);
            levelStatement.addBatch();

            PreparedStatement itemStatement = database.prepareStatement(insertItemQuery);
            PreparedStatement materialStatement = database.prepareStatement(insertMaterialQuery);

            for (Map.Entry<ItemAction, List<SimpleItemStack>> entry : itemsMap.entrySet()) {
                for (SimpleItemStack item : entry.getValue()) {
                    if (item.isEmpty()) {
                        continue;
                    }
                    ResourceLocation itemLocation = item.getItem().arch$registryName();
                    if (itemLocation != null) {
                        String materialName = itemLocation.toString().replace("minecraft:", "");
                        materialStatement.setString(1, materialName);
                        materialStatement.addBatch();

                        itemStatement.setLong(1, time);
                        itemStatement.setString(2, userUuid);
                        itemStatement.setString(3, levelName);
                        itemStatement.setInt(4, x);
                        itemStatement.setInt(5, y);
                        itemStatement.setInt(6, z);
                        itemStatement.setString(7, materialName);
                        itemStatement.setBytes(8, item.getTagBytes(level));
                        itemStatement.setInt(9, item.getCount());
                        itemStatement.setInt(10, entry.getKey().getId());
                        itemStatement.addBatch();
                    }
                }
            }
            database.batchQueue.add(levelStatement);
            database.batchQueue.add(materialStatement);
            database.batchQueue.add(itemStatement);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to insert item", e);
        }
    }

    public List<IHistory> getHistory(Level level, int x, int y, int z) {
        List<IHistory> containerHistory = new ArrayList<>();
        String query = isMysql() ? ContainerSql.CONTAINER_HISTORY_BY_POSITION_MYSQL : ContainerSql.CONTAINER_HISTORY_BY_POSITION_SQLITE;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, level.dimension().location().toString());
            preparedStatement.setInt(2, x);
            preparedStatement.setInt(3, y);
            preparedStatement.setInt(4, z);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ByteBuf buf1 = Unpooled.wrappedBuffer(resultSet.getBytes(8));
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(buf1, level.registryAccess());
                DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
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
        String query = isMysql() ? ContainerSql.CONTAINER_HISTORY_BY_RANGE_MYSQL : ContainerSql.CONTAINER_HISTORY_BY_RANGE_SQLITE;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, level.dimension().location().toString());
            preparedStatement.setInt(2, x);
            preparedStatement.setInt(3, x2);
            preparedStatement.setInt(4, y);
            preparedStatement.setInt(5, y2);
            preparedStatement.setInt(6, z);
            preparedStatement.setInt(7, z2);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ByteBuf buf1 = Unpooled.wrappedBuffer(resultSet.getBytes(8));
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(buf1, level.registryAccess());
                DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
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
                AND containers.x BETWEEN ? AND ?
                AND containers.y BETWEEN ? AND ?
                AND containers.z BETWEEN ? AND ?
                ORDER BY containers.time DESC
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

            preparedStatement.setInt(7, filterList.getRadiusMinX());
            preparedStatement.setInt(8, filterList.getRadiusMaxX());
            preparedStatement.setInt(9, filterList.getRadiusMinY());
            preparedStatement.setInt(10, filterList.getRadiusMaxY());
            preparedStatement.setInt(11, filterList.getRadiusMinZ());
            preparedStatement.setInt(12, filterList.getRadiusMaxZ());

            List<IHistory> blockHistory = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ByteBuf buf1 = Unpooled.wrappedBuffer(resultSet.getBytes(8));
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(buf1, level.registryAccess());
                DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
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
