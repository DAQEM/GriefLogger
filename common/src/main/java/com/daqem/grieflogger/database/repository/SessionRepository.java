package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.command.filter.*;
import com.daqem.grieflogger.config.GriefLoggerCommonConfig;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.cache.Caches;
import com.daqem.grieflogger.model.action.IAction;
import com.daqem.grieflogger.model.action.SessionAction;
import com.daqem.grieflogger.model.history.SessionHistory;
import org.jetbrains.annotations.Nullable;

import javax.sql.rowset.serial.SerialArray;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SessionRepository extends Repository {

    private final Database database;

    public SessionRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS sessions (
                    time integer NOT NULL,
                    user integer NOT NULL,
                    level integer NOT NULL,
                    x integer NOT NULL,
                    y integer NOT NULL,
                    z integer NOT NULL,
                    action integer NOT NULL,
                    FOREIGN KEY(user) REFERENCES users(id),
                    FOREIGN KEY(level) REFERENCES levels(id)
                );
                """;
        if (isMysql()) {
            sql = """
                    CREATE TABLE IF NOT EXISTS sessions (
                        time bigint NOT NULL,
                        user int NOT NULL,
                        level int NOT NULL,
                        x int NOT NULL,
                        y int NOT NULL,
                        z int NOT NULL,
                        action int NOT NULL,
                        FOREIGN KEY(user) REFERENCES users(id),
                        FOREIGN KEY(level) REFERENCES levels(id)
                    )
                    ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4;
                    """;
        }
        database.createTable(sql);
    }

    public void insert(long time, String userUuid, String levelName, int x, int y, int z, int sessionAction) {
        String query = """
                INSERT OR IGNORE INTO sessions(time, user, level, x, y, z, action)
                VALUES(?, (
                    SELECT id FROM users WHERE uuid = ?
                ), (
                    SELECT id FROM levels WHERE name = ?
                ), ?, ?, ?, ?);
                """;

        if (isMysql()) {
            query = """
                    INSERT IGNORE INTO sessions(time, user, level, x, y, z, action)
                    VALUES(?, (
                        SELECT id FROM users WHERE uuid = ?
                    ), (
                        SELECT id FROM levels WHERE name = ?
                    ), ?, ?, ?, ?);
                    """;
        }

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setLong(1, time);
            preparedStatement.setString(2, userUuid);
            preparedStatement.setString(3, levelName);
            preparedStatement.setInt(4, x);
            preparedStatement.setInt(5, y);
            preparedStatement.setInt(6, z);
            preparedStatement.setInt(7, sessionAction);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert session into database", exception);
        }
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
                AND sessions.x BETWEEN ? AND ?
                AND sessions.y BETWEEN ? AND ?
                AND sessions.z BETWEEN ? AND ?
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

            preparedStatement.setInt(5, filterList.getRadiusMinX());
            preparedStatement.setInt(6, filterList.getRadiusMaxX());
            preparedStatement.setInt(7, filterList.getRadiusMinY());
            preparedStatement.setInt(8, filterList.getRadiusMaxY());
            preparedStatement.setInt(9, filterList.getRadiusMinZ());
            preparedStatement.setInt(10, filterList.getRadiusMaxZ());

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
