package com.daqem.grieflogger.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.daqem.grieflogger.database.dialect.MySQLDialect;
import com.supermartijn642.configlib.ConfigLib;
import org.jetbrains.annotations.Nullable;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.config.GriefLoggerConfig;
import com.daqem.grieflogger.database.dialect.IDatabaseDialect;
import com.daqem.grieflogger.database.dialect.SQLiteDialect;
import com.daqem.grieflogger.database.queue.IQueue;
import com.daqem.grieflogger.database.queue.Queue;
import com.daqem.grieflogger.database.queue.SqlTask;

public class Database {

    @Nullable
    private Connection connection;
    public final IQueue queue;
    public final IQueue batchQueue;
    private IDatabaseDialect dialect;
    private final Object lock = new Object();

    public Database() {
        queue = new Queue(this, false);
        batchQueue = new Queue(this, true);
    }

    public boolean createConnection() {
        boolean connected;
        if (GriefLoggerConfig.useMysql.get()) {
            connected = createMysqlConnection();
            dialect = new MySQLDialect();
        } else {
            connected = createSqliteConnection();
            dialect = new SQLiteDialect();
        }
        if (connection != null) {
            GriefLogger.LOGGER.info("Connected to database");
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                GriefLogger.LOGGER.error("Failed to set auto commit", e);
                return false;
            }
        }
        return connected && connection != null;
    }

    public boolean createMysqlConnection() {
        String host = GriefLoggerConfig.mysqlHost.get();
        int port = GriefLoggerConfig.mysqlPort.get();
        String database = GriefLoggerConfig.mysqlDatabase.get();
        String user = GriefLoggerConfig.mysqlUsername.get();
        String password = GriefLoggerConfig.mysqlPassword.get();
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?allowReconnect=true&autoReconnect=true&connectTimeout=" + GriefLoggerConfig.mysqlTimeout.get();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            GriefLogger.LOGGER.error("Failed to load MySQL driver", e);
            return false;
        }
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to connect to MySQL database", e);
            return false;
        }
        return connection != null;
    }

    public boolean createSqliteConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            GriefLogger.LOGGER.error("Failed to load SQLite driver", e);
            return false;
        }
        Path path = ConfigLib.getConfigFolder().toPath().resolve(GriefLogger.MOD_ID);
        if (!path.toFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            path.toFile().mkdirs();
        }
        try {
            String dbPath = path.resolve("database.db").toString();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to connect to SQLite database", e);
            return false;
        }
        return connection != null;
    }

    public void createTable(String sql) {
        execute(sql, true);
    }

    public void execute(String sql, boolean logError) {
        if (connection == null) return;

        synchronized (lock) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
                connection.commit();
            } catch (SQLException e) {
                if (logError) {
                    GriefLogger.LOGGER.error("Failed to execute statement", e);
                }
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    GriefLogger.LOGGER.error("Failed to rollback", ex);
                }
            }
        }
    }

    public PreparedStatement prepareStatement(String query) throws SQLException {
        if (connection != null) {
            return connection.prepareStatement(query);
        } else {
            throw new SQLException("Connection is null");
        }
    }

    public void executeQueue(List<Object> items, boolean isBatch) {
        if (connection == null) return;

        synchronized (lock) {
            try {
                for (Object item : items) {
                    if (item instanceof PreparedStatement preparedStatement) {
                        if (preparedStatement.isClosed()) {
                            continue;
                        }
                        try (preparedStatement) {
                            if (isBatch) {
                                preparedStatement.executeBatch();
                            } else {
                                preparedStatement.executeUpdate();
                            }
                        }
                    } else if (item instanceof SqlTask task) {
                        task.execute(connection);
                    }
                }
                if (!items.isEmpty()) {
                    if (connection != null && !connection.isClosed()) {
                        connection.commit();
                    }
                }
            } catch (SQLException e) {
                GriefLogger.LOGGER.error("Failed to execute database queue", e);
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.rollback();
                    }
                } catch (SQLException ex) {
                    GriefLogger.LOGGER.error("Failed to rollback transaction", ex);
                }
            }
        }
    }

    public IDatabaseDialect getDialect() {
        return dialect;
    }
}