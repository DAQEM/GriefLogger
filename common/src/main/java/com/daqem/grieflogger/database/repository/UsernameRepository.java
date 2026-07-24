package com.daqem.grieflogger.database.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.dialect.MySQLDialect;

public class UsernameRepository extends Repository {

    private final Database database;

    public UsernameRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS usernames (" +
                "id " + database.getDialect().getDataType("integer") + " PRIMARY KEY" + (database.getDialect() instanceof MySQLDialect ? " AUTO_INCREMENT" : "") + "," +
                "time " + database.getDialect().getDataType("bigint") + " NOT NULL," +
                "uuid " + database.getDialect().getDataType("varchar") + "(36) NOT NULL," +
                "name " + database.getDialect().getDataType("varchar") + "(16) NOT NULL," +
                "UNIQUE(uuid, name)" +
                ")";
        if (database.getDialect() instanceof MySQLDialect) {
            sql += " ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4;";
        } else {
            sql += ";";
        }
        database.createTable(sql);
    }

    public void insert(long time, String uuid, String name) {
        String query = database.getDialect().getInsertIgnore() + " INTO usernames(time, uuid, name) VALUES(?, ?, ?);";

        database.queue.add(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, time);
                preparedStatement.setString(2, uuid);
                preparedStatement.setString(3, name);
                preparedStatement.executeUpdate();
            }
        });
    }
}