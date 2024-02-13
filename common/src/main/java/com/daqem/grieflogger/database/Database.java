package com.daqem.grieflogger.database;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.GriefLoggerExpectPlatform;
import com.daqem.grieflogger.config.GriefLoggerConfig;
import com.daqem.grieflogger.database.queue.IQueue;
import com.daqem.grieflogger.database.queue.Queue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public class Database {

    private Connection connection;
    private Statement statement;
    public final IQueue queue;
    public final IQueue batchQueue;

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
        queue = new Queue(this, false);
        batchQueue = new Queue(this, true);
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to set auto commit", e);
        }
    }

    public void createTable(String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to create table", e);
        }
    }

    public void execute(String sql, boolean logError) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            if (logError) {
                GriefLogger.LOGGER.error("Failed to execute statement", e);
            }
        }
    }

    public PreparedStatement prepareStatement(String query) throws SQLException {
        return connection.prepareStatement(query);
    }

    public void executeStatements(List<PreparedStatement> statements, boolean isBatch) {
        try {
            for (PreparedStatement statement : statements) {
                if (statement == null) {
                    GriefLogger.LOGGER.error("Statement is null");
                    continue;
                }

                if (statement.isClosed()) {
                    GriefLogger.LOGGER.error("Statement is closed");
                    continue;
                }

                try (statement) {
                    if (isBatch) {
                        statement.executeBatch(); // Execute as a batch
                    } else {
                        statement.executeUpdate(); // Execute individually
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to execute statements", e);
        }
    }
}
