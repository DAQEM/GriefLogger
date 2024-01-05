package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.database.Database;

public class BlockRepository {

    private final Database database;

    public BlockRepository(Database database) {
        this.database = database;
    }

    public void createTable() {
        database.execute("""
                CREATE TABLE IF NOT EXISTS blocks (
                	time integer NOT NULL,
                	user integer NOT NULL,
                	level integer NOT NULL,
                	x integer NOT NULL,
                	y integer NOT NULL,
                	z integer NOT NULL,
                	material integer NOT NULL,
                	action integer NOT NULL
                );
                """);
    }

    public void insert(long time, int user, int level, int x, int y, int z, int material, int action) {
        database.executeUpdate("""
                INSERT INTO blocks(time, user, level, x, y, z, material, action)
                VALUES(%d, %d, %d, %d, %d, %d, %d, %d)
                """.formatted(time, user, level, x, y, z, material, action));
    }
}
