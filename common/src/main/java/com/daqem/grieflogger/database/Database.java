package com.daqem.grieflogger.database;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.GriefLoggerExpectPlatform;
import com.daqem.grieflogger.config.GriefLoggerConfig;

import java.nio.file.Path;
import java.sql.*;

public class Database {

    private Connection connection;
    private Statement statement;

    public Database(String dbPath) {
        try {
            if (GriefLoggerConfig.useMysql.get()) {
                String host = GriefLoggerConfig.mysqlHost.get();
                int port = GriefLoggerConfig.mysqlPort.get();
                String database = GriefLoggerConfig.mysqlDatabase.get();
                String user = GriefLoggerConfig.mysqlUsername.get();
                String password = GriefLoggerConfig.mysqlPassword.get();
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, user, password);
            } else {
                Class.forName("org.sqlite.JDBC");
                Path path = GriefLoggerExpectPlatform.getConfigDirectory().resolve(GriefLogger.MOD_ID);
                if (!path.toFile().exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    path.toFile().mkdirs();
                }
                String url = "jdbc:sqlite:" + path.resolve(dbPath).toString();
                connection = DriverManager.getConnection(url);
            }
            statement = connection.createStatement();
            GriefLogger.LOGGER.info("Connected to database");
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
