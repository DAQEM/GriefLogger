package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.database.Database;

import java.util.Optional;

public class UsernameRepository {

    private final Database database;

    public UsernameRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.execute("""
                CREATE TABLE IF NOT EXISTS usernames (
                	id integer PRIMARY KEY AUTOINCREMENT,
                	time integer NOT NULL,
                	uuid text NOT NULL,
                	name text NOT NULL,
                	UNIQUE(uuid, name)
                );
                """);
    }

    public void insert(long time, String uuid, String name) {
        database.executeUpdate("""
                INSERT INTO usernames(time, uuid, name)
                VALUES(%d, '%s', '%s')
                ON CONFLICT(uuid, name)
                DO NOTHING
                """.formatted(time, uuid, name));
    }

    public Optional<String> getName(String uuid) {
        String sql = "SELECT name FROM usernames WHERE uuid = '" + uuid + "' ORDER BY time DESC LIMIT 1";
        return database.getString(sql, "name");
    }
}
