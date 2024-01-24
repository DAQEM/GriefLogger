package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.config.GriefLoggerCommonConfig;
import com.daqem.grieflogger.database.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MaterialRepository extends Repository {

    private final Database database;

    public MaterialRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS materials (
                	id integer PRIMARY KEY,
                	name text NOT NULL UNIQUE
                );
                """;
        if (isMysql()) {
            sql = """
                    CREATE TABLE IF NOT EXISTS materials (
                    	id int PRIMARY KEY AUTO_INCREMENT,
                    	name varchar(256) NOT NULL UNIQUE
                    )
                    ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4;
                    """;
        }
        database.createTable(sql);
    }

    public void insert(String material) {
        String query = """
                INSERT OR IGNORE INTO materials(name)
                VALUES(?);
                """;

        if (isMysql()) {
            query = """
                    INSERT IGNORE INTO materials(name)
                    VALUES(?);
                    """;
        }

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, material);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            GriefLogger.LOGGER.error("Failed to insert material into database", exception);
        }
    }
}
