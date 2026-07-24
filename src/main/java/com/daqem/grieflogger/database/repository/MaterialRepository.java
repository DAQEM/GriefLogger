package com.daqem.grieflogger.database.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.dialect.MySQLDialect;

public class MaterialRepository extends Repository {

    private final Database database;

    public MaterialRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS materials (" +
                "id " + database.getDialect().getDataType("integer") + " PRIMARY KEY" + (database.getDialect() instanceof MySQLDialect ? " AUTO_INCREMENT" : "") + "," +
                "name " + database.getDialect().getDataType("text") + " NOT NULL UNIQUE" +
                ")";
        if (database.getDialect() instanceof MySQLDialect) {
            sql += " ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4;";
        } else {
            sql += ";";
        }
        database.createTable(sql);
    }

    public void insert(String material) {
        String query = database.getDialect().getInsertIgnore() + " INTO materials(name) VALUES(?);";

        database.queue.add(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, material);
                preparedStatement.executeUpdate();
            }
        });
    }
}