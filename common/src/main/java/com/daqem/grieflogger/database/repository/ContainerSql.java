package com.daqem.grieflogger.database.repository;

/**
 * Centralized SQL for container (chest/barrel/etc.) transaction persistence and position lookups.
 *
 * <p>Kept free of any Minecraft/mod types so the exact statements the repository emits can be
 * exercised directly against a database in tests. Two dialect variants are provided where they
 * differ: SQLite (default backend) uses {@code INSERT OR IGNORE}; MySQL uses {@code INSERT IGNORE}.
 *
 * <p>Reliability (GAP E): the container insert is a plain {@code INSERT} that resolves its
 * {@code NOT NULL} foreign keys ({@code level}/{@code user}/{@code type}) via scalar sub-selects.
 * A missing parent row makes a sub-select resolve to {@code NULL}, which violates the column and
 * throws — aborting the whole flush batch's commit. Callers MUST upsert the parent rows (level,
 * user, material) before the transaction insert so the sub-selects resolve. Position lookups use
 * the standard {@code JOIN levels ON containers.level = levels.id WHERE levels.name = ?} form
 * rather than a sub-select join.
 */
public final class ContainerSql {

    private ContainerSql() {
    }

    // --- parent upserts (idempotent; run before the transaction insert) ---

    public static final String MATERIAL_UPSERT_SQLITE = "INSERT OR IGNORE INTO materials(name) VALUES(?)";
    public static final String MATERIAL_UPSERT_MYSQL = "INSERT IGNORE INTO materials(name) VALUES(?)";

    public static final String LEVEL_UPSERT_SQLITE = "INSERT OR IGNORE INTO levels(name) VALUES(?)";
    public static final String LEVEL_UPSERT_MYSQL = "INSERT IGNORE INTO levels(name) VALUES(?)";

    // --- transaction insert (resolves FKs via sub-select; parents must already be upserted) ---
    // Params: 1=time, 2=uuid, 3=levelName, 4=x, 5=y, 6=z, 7=material, 8=data, 9=amount, 10=action

    public static final String CONTAINER_INSERT_SQLITE = """
            INSERT INTO containers(time, user, level, x, y, z, type, data, amount, action)
            VALUES(?, (SELECT id FROM users WHERE uuid = ?), (SELECT id FROM levels WHERE name = ?), ?, ?, ?, (SELECT id FROM materials WHERE name = ?), ?, ?, ?)""";
    public static final String CONTAINER_INSERT_MYSQL = CONTAINER_INSERT_SQLITE;

    // --- normalized position lookups (dialect-neutral) ---
    // Single position params: 1=levelName, 2=x, 3=y, 4=z
    // Range params:           1=levelName, 2=x1, 3=x2, 4=y1, 5=y2, 6=z1, 7=z2

    private static final String HISTORY_SELECT = """
            SELECT containers.time, users.name, users.uuid, containers.x, containers.y, containers.z, materials.name, containers.data, containers.amount, containers.action
            FROM containers
            INNER JOIN users ON containers.user = users.id
            INNER JOIN levels ON containers.level = levels.id
            INNER JOIN materials ON containers.type = materials.id
            WHERE levels.name = ?""";

    public static final String CONTAINER_HISTORY_BY_POSITION_SQLITE = HISTORY_SELECT
            + " AND containers.x = ? AND containers.y = ? AND containers.z = ?"
            + " AND (containers.action = 0 OR containers.action = 1)\nORDER BY containers.time DESC";
    public static final String CONTAINER_HISTORY_BY_POSITION_MYSQL = CONTAINER_HISTORY_BY_POSITION_SQLITE;

    public static final String CONTAINER_HISTORY_BY_RANGE_SQLITE = HISTORY_SELECT
            + " AND containers.x BETWEEN ? AND ? AND containers.y BETWEEN ? AND ? AND containers.z BETWEEN ? AND ?"
            + " AND (containers.action = 0 OR containers.action = 1)\nORDER BY containers.time DESC";
    public static final String CONTAINER_HISTORY_BY_RANGE_MYSQL = CONTAINER_HISTORY_BY_RANGE_SQLITE;
}
