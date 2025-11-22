package com.daqem.grieflogger.database.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;

public class UserRepository extends Repository {

    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id " + database.getDialect().getDataType("integer") + " PRIMARY KEY" + (database.getDialect() instanceof com.daqem.grieflogger.database.dialect.MySQLDialect ? " AUTO_INCREMENT" : "") + "," +
                "name " + database.getDialect().getDataType("text") + " NOT NULL," +
                "uuid " + database.getDialect().getDataType("text") + " DEFAULT NULL UNIQUE" +
                ")";
        if (database.getDialect() instanceof com.daqem.grieflogger.database.dialect.MySQLDialect) {
            sql += " ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4;";
        } else {
            sql += ";";
        }
        database.createTable(sql);
    }

    public void insertOrUpdateName(String name, String uuid) {
        String query = "INSERT INTO users(name, uuid) VALUES(?, ?) " +
                database.getDialect().getOnConflictUpdate("uuid", "name = ?");

        try {
            PreparedStatement preparedStatement = database.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, uuid);
            preparedStatement.setString(3, name);
            database.queue.add(preparedStatement);
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert username into database", exception);
        }
    }

    public void insertNonPlayer(String name) {
        String query = "INSERT INTO users(name) VALUES('%s') " +
                database.getDialect().getOnConflictDoNothing("name");

        try {
            PreparedStatement preparedStatement = database.prepareStatement(query);
            preparedStatement.setString(1, name);
            database.queue.add(preparedStatement);
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert username into database", exception);
        }
    }

    public Map<Integer, String> getAllUsernames() {
        Map<Integer, String> usernames = new HashMap<>();
        String query = """
                SELECT id, name FROM users
                """;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                usernames.put(
                        resultSet.getInt(1),
                        resultSet.getString(2)
                );
            }
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to get all usernames from database", exception);
        }
        return usernames;
    }
}
