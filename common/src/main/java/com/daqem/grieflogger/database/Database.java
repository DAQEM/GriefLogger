package com.daqem.grieflogger.database;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.GriefLoggerExpectPlatform;

import java.nio.file.Path;
import java.sql.*;
import java.util.Optional;

public class Database {

    private Connection connection = null;
    private Statement statement = null;
    private ResultSet resultSet = null;

    public Database(String dbPath) {
        try {
            Class.forName("org.sqlite.JDBC");
            Path path = GriefLoggerExpectPlatform.getConfigDirectory().resolve(GriefLogger.MOD_ID);
            if (!path.toFile().exists()) {
                //noinspection ResultOfMethodCallIgnored
                path.toFile().mkdirs();
            }
            String url = "jdbc:sqlite:" + path.resolve(dbPath).toString();
            GriefLogger.LOGGER.info("Database URL: " + url);
            connection = DriverManager.getConnection(url);
            statement = connection.createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            GriefLogger.LOGGER.error("Failed to connect to database", e);
        }
    }

    public ResultSet executeQuery(String sql) {
        try {
            resultSet = statement.executeQuery(sql);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to execute query", e);
        }
        return resultSet;
    }

    public void execute(String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to execute", e);
        }
    }

    public void executeUpdate(String sql) {
        try {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to execute update", e);
        }
    }

    public void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to close database", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public Optional<Integer> getId(String sql) {
        ResultSet resultSet = executeQuery(sql);
        try {
            if (resultSet.next()) {
                return Optional.of(resultSet.getInt("id"));
            }
        } catch (Exception e) {
            GriefLogger.LOGGER.error("Failed to get id from table", e);
        }
        return Optional.empty();
    }

    public Optional<String> getString(String sql, String column) {
        ResultSet resultSet = executeQuery(sql);
        try {
            if (resultSet.next()) {
                return Optional.of(resultSet.getString(column));
            }
        } catch (Exception e) {
            GriefLogger.LOGGER.error("Failed to get string from table", e);
        }
        return Optional.empty();
    }
}
