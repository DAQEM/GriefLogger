package com.daqem.grieflogger.database.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.daqem.grieflogger.database.dialect.MySQLDialect;
import org.jetbrains.annotations.Nullable;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.command.filter.FilterList;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.model.history.SessionHistory;

public class SessionRepository extends Repository {

    private final Database database;

    public SessionRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS sessions (" +
                "time " + database.getDialect().getDataType("bigint") + " NOT NULL," +
                "user " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "level " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "x " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "y " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "z " + database.getDialect().getDataType("integer") + " NOT NULL," +
                "action " + database.getDialect().getDataType("integer") + " NOT NULL," +
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
            sql = "ALTER TABLE sessions ADD INDEX coordinates (x, y, z);";
        } else {
            sql = "CREATE INDEX IF NOT EXISTS coordinates ON sessions (x, y, z);";
        }
        database.execute(sql, false);
    }

    public void insert(long time, String userUuid, String levelName, int x, int y, int z, int sessionAction) {
        String query = database.getDialect().getInsertIgnore() + " INTO sessions(time, user, level, x, y, z, action) " +
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
                preparedStatement.setInt(7, sessionAction);
                preparedStatement.executeUpdate();
            }
        });
    }

    public List<SessionHistory> getFilteredSessionHistory(String levelName, FilterList filterList) {
        @Nullable String actions = filterList.getActionString();
        @Nullable String users = filterList.getUserString();

        String query = """
                SELECT sessions.time, users.name, users.uuid, sessions.x, sessions.y, sessions.z, sessions.action
                FROM sessions
                INNER JOIN users ON sessions.user = users.id
                INNER JOIN levels ON sessions.level = levels.id
                WHERE levels.name = ?
                AND sessions.time > ?
                AND (? IS NULL OR sessions.action IN (%s))
                AND (? IS NULL OR users.id IN (%s))
                AND (? IS NULL OR sessions.x BETWEEN ? AND ?)
                AND (? IS NULL OR sessions.y BETWEEN ? AND ?)
                AND (? IS NULL OR sessions.z BETWEEN ? AND ?)
                ORDER BY sessions.time DESC
                LIMIT 1000;
                """.formatted(actions, users);

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, levelName);
            preparedStatement.setLong(2, filterList.getTime());

            if (actions == null || actions.isEmpty()) {
                preparedStatement.setNull(3, Types.VARCHAR);
            } else {
                preparedStatement.setString(3, actions);
            }

            if (users == null || users.isEmpty()) {
                preparedStatement.setNull(4, Types.VARCHAR);
            } else {
                preparedStatement.setString(4, users);
            }

            if (filterList.getRadiusFilter().isEmpty()) {
                preparedStatement.setNull(5, Types.VARCHAR);
            } else {
                preparedStatement.setString(5, "not null");
            }

            preparedStatement.setInt(6, filterList.getRadiusMinX());
            preparedStatement.setInt(7, filterList.getRadiusMaxX());

            if (filterList.getRadiusFilter().isEmpty()) {
                preparedStatement.setNull(8, Types.VARCHAR);
            } else {
                preparedStatement.setString(8, "not null");
            }

            preparedStatement.setInt(9, filterList.getRadiusMinY());
            preparedStatement.setInt(10, filterList.getRadiusMaxY());

            if (filterList.getRadiusFilter().isEmpty()) {
                preparedStatement.setNull(11, Types.VARCHAR);
            } else {
                preparedStatement.setString(11, "not null");
            }

            preparedStatement.setInt(12, filterList.getRadiusMinZ());
            preparedStatement.setInt(13, filterList.getRadiusMaxZ());

            List<SessionHistory> sessionHistory = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sessionHistory.add(new SessionHistory(
                        resultSet.getLong("time"),
                        resultSet.getString("name"),
                        resultSet.getString("uuid"),
                        resultSet.getInt("x"),
                        resultSet.getInt("y"),
                        resultSet.getInt("z"),
                        resultSet.getInt("action")));
            }
            return sessionHistory;
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to get session history from database", exception);
            return List.of();
        }
    }
}