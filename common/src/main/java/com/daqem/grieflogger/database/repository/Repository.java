package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.dialect.MySQLDialect;

public abstract class Repository implements IRepository {

    /**
     * Creates the indexes shared by the six logged tables, which all key on
     * (time, user, level, x, y, z).
     * <p>
     * {@code <table>_position} covers positional lookups. It leads with {@code level} because
     * Overworld and Nether coordinates overlap heavily, and ends with {@code time} so that the
     * {@code ORDER BY time DESC} those lookups carry comes from the index rather than a sort.
     * {@code <table>_level_time} serves time-window queries, which previously had no index at all
     * and scanned the whole table.
     * <p>
     * These replace a {@code coordinates(x, y, z)} index, which besides missing {@code level} used
     * one name for all six tables. SQLite scopes index names per database rather than per table,
     * so only the first table to reach this method ever received one. The names here are per table
     * so that they create alongside it; dropping the old index is a one time job and wants a
     * migration rather than this method, which runs on every startup.
     */
    protected void createPositionIndexes(Database database, String table) {
        if (database.getDialect() instanceof MySQLDialect) {
            database.execute("ALTER TABLE " + table + " ADD INDEX " + table + "_position (level, x, y, z, time);", false);
            database.execute("ALTER TABLE " + table + " ADD INDEX " + table + "_level_time (level, time);", false);
        } else {
            database.execute("CREATE INDEX IF NOT EXISTS " + table + "_position ON " + table + " (level, x, y, z, time);", false);
            database.execute("CREATE INDEX IF NOT EXISTS " + table + "_level_time ON " + table + " (level, time);", false);
        }
    }
}
