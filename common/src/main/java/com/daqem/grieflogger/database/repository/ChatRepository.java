package com.daqem.grieflogger.database.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.dialect.MySQLDialect;

public class ChatRepository extends Repository {

    private final Database database;

    public ChatRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS chats (" +
                "time " + database.getDialect().getDataType("bigint") + " NOT NULL," +
                "user " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "level " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "x " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "y " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "z " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "message " + database.getDialect().getDataType("varchar") + "(256) NOT NULL," +
                "FOREIGN KEY(user) REFERENCES users(id)," +
                "FOREIGN KEY(level) REFERENCES levels(id)" +
                ")";
        if (database.getDialect() instanceof MySQLDialect) {
            sql += " ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4;";
        } else {
            sql += ";";
        }
        database.createTable(sql);
    }

    public void createIndexes() {
        String sql;
        if (database.getDialect() instanceof MySQLDialect) {
            sql = "ALTER TABLE chats ADD INDEX coordinates (x, y, z);";
        } else {
            sql = "CREATE INDEX IF NOT EXISTS coordinates ON chats (x, y, z);";
        }
        database.execute(sql, false);
    }

    public void insert(long time, String userUuid, String levelName, int x, int y, int z, String message) {
        String query = database.getDialect().getInsertIgnore() + " INTO chats(time, user, level, x, y, z, message) " +
                "VALUES(?, (" +
                "SELECT id FROM users WHERE uuid = ?" +
                "), (" +
                "SELECT id FROM levels WHERE name = ?" +
                "), ?, ?, ?, ?);";

        database.queue.add(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, time);
                preparedStatement.setString(2, userUuid);
                preparedStatement.setString(3, levelName);
                preparedStatement.setInt(4, x);
                preparedStatement.setInt(5, y);
                preparedStatement.setInt(6, z);
                preparedStatement.setString(7, message);
                preparedStatement.executeUpdate();
            }
        });
    }
}