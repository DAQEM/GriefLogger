package com.daqem.grieflogger.database;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.GriefLoggerExpectPlatform;

import java.nio.file.Path;
import java.sql.*;

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
            connection = DriverManager.getConnection(url);
            statement = connection.createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            GriefLogger.LOGGER.error("Failed to connect to database", e);
        }
    }

    public void createTable(String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to create table", e);
        }
    }

    public PreparedStatement prepareStatement(String query) throws SQLException {
        return connection.prepareStatement(query);
    }
}
