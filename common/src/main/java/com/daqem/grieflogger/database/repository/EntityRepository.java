package com.daqem.grieflogger.database.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;

public class EntityRepository extends Repository {

    private final Database database;

    public EntityRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS entities (" +
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
        String query = database.getDialect().getInsertIgnore() + " INTO entities(name) VALUES(?);";

        try {
            PreparedStatement preparedStatement = database.prepareStatement(query);
            preparedStatement.setString(1, name);
            database.queue.add(preparedStatement);
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert entity into database", exception);
        }
    }
}
