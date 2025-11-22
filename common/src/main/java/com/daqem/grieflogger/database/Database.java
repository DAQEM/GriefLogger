package com.daqem.grieflogger.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.daqem.yamlconfig.YamlConfigExpectPlatform;
import org.jetbrains.annotations.Nullable;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.config.GriefLoggerConfig;
import com.daqem.grieflogger.database.dialect.IDatabaseDialect;
import com.daqem.grieflogger.database.dialect.MySQLDialect;
import com.daqem.grieflogger.database.dialect.SQLiteDialect;
import com.daqem.grieflogger.database.queue.IQueue;
import com.daqem.grieflogger.database.queue.Queue;

public class Database {

    @Nullable
    private Connection connection;
    @Nullable
    private Statement statement;
    public final IQueue queue;
    public final IQueue batchQueue;
    private IDatabaseDialect dialect;

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
                statement = connection.createStatement();
            } catch (SQLException e) {
                GriefLogger.LOGGER.error("Failed to create statement", e);
                return false;
            }
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                GriefLogger.LOGGER.error("Failed to set auto commit", e);
                return false;
            }
        }
        return connected && connection != null && statement != null;
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
        Path path = YamlConfigExpectPlatform.getConfigDirectory().resolve(GriefLogger.MOD_ID);
        if (!path.toFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            path.toFile().mkdirs();
        }
        try {
            // Construct the JDBC URL with the full path to database.db
            String dbPath = path.resolve("database.db").toString();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to connect to SQLite database", e);
            return false;
        }
        return connection != null;
    }

    public void createTable(String sql) {
        try {
            if (statement != null) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to create table", e);
        }
    }

    public void execute(String sql, boolean logError) {
        try {
            if (statement != null) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            if (logError) {
                GriefLogger.LOGGER.error("Failed to execute statement", e);
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
            if (!statements.isEmpty()) {
                if (connection != null) {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            GriefLogger.LOGGER.error("Failed to execute statements", e);
        }
    }

    public IDatabaseDialect getDialect() {
        return dialect;
    }
}
