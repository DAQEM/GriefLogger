package com.daqem.grieflogger.database.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;

public class LevelRepository extends Repository {

    private final Database database;

    public LevelRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS levels (" +
                "id " + database.getDialect().getDataType("integer") + " PRIMARY KEY" + (database.getDialect() instanceof com.daqem.grieflogger.database.dialect.MySQLDialect ? " AUTO_INCREMENT" : "") + "," +
                "name " + database.getDialect().getDataType("text") + " NOT NULL UNIQUE" +
                ")";
        if (database.getDialect() instanceof com.daqem.grieflogger.database.dialect.MySQLDialect) {
            sql += " ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4;";
        } else {
            sql += ";";
        }
        database.createTable(sql);
    }

    public void insert(String name) {
        String query = database.getDialect().getInsertIgnore() + " INTO levels(name) VALUES(?);";

        try {
            PreparedStatement preparedStatement = database.prepareStatement(query);
            preparedStatement.setString(1, name);
            database.queue.add(preparedStatement);
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert level into database", exception);
        }
    }
}
