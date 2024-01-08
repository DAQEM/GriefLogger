package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UsernameRepository {

    private final Database database;

    public UsernameRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.createTable("""
                CREATE TABLE IF NOT EXISTS usernames (
                	id integer PRIMARY KEY,
                	time integer NOT NULL,
                	uuid text NOT NULL,
                	name text NOT NULL,
                	UNIQUE(uuid, name)
                );
                """);
    }

    public void insert(long time, String uuid, String name) {
        String query = """
                INSERT OR IGNORE INTO usernames(time, uuid, name)
                VALUES(?, ?, ?);
                """;

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setLong(1, time);
            preparedStatement.setString(2, uuid);
            preparedStatement.setString(3, name);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert username into database", exception);
        }
    }
}
