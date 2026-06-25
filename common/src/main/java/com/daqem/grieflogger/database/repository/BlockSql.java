package com.daqem.grieflogger.database.repository;

/**
 * Centralized SQL for block/entity persistence and position lookups.
 *
 * <p>Kept free of any Minecraft/mod types so the exact statements the repositories emit can be
 * exercised directly against a database in tests. Two dialect variants are provided where they
 * differ: SQLite (default backend) uses {@code INSERT OR IGNORE}; MySQL uses {@code INSERT IGNORE}.
 *
 * <p>Reliability (GAP E): a logged event is dropped when a {@code NOT NULL} foreign key
 * ({@code level}/{@code user}/{@code type}) resolves to {@code NULL} because its parent row did not
 * exist yet. Callers MUST upsert the parent rows (level, user, material/entity) before the event
 * insert so the sub-selects resolve. Position lookups use the standard
 * {@code JOIN levels ON blocks.level = levels.id WHERE levels.name = ?} form so a missing/late
 * level row cannot zero out results for events that are stored.
 */
public final class BlockSql {

    private BlockSql() {
    }

    // --- parent upserts (idempotent; run before the event insert) ---

    public static final String MATERIAL_UPSERT_SQLITE = "INSERT OR IGNORE INTO materials(name) VALUES(?)";
    public static final String MATERIAL_UPSERT_MYSQL = "INSERT IGNORE INTO materials(name) VALUES(?)";

    public static final String ENTITY_UPSERT_SQLITE = "INSERT OR IGNORE INTO entities(name) VALUES(?)";
    public static final String ENTITY_UPSERT_MYSQL = "INSERT IGNORE INTO entities(name) VALUES(?)";

    public static final String LEVEL_UPSERT_SQLITE = "INSERT OR IGNORE INTO levels(name) VALUES(?)";
    public static final String LEVEL_UPSERT_MYSQL = "INSERT IGNORE INTO levels(name) VALUES(?)";

    // users.name is NOT NULL, so the upsert carries the player name; keyed on the unique uuid.
    // Params: 1=name, 2=uuid, 3=name (conflict update)
    public static final String USER_UPSERT_SQLITE =
            "INSERT INTO users(name, uuid) VALUES(?, ?) ON CONFLICT(uuid) DO UPDATE SET name = ?";
    public static final String USER_UPSERT_MYSQL =
            "INSERT INTO users(name, uuid) VALUES(?, ?) ON DUPLICATE KEY UPDATE name = ?";

    // --- event inserts (resolve FKs via sub-select; parents must already be upserted) ---
    // Params: 1=time, 2=uuid, 3=levelName, 4=x, 5=y, 6=z, 7=material|entity, 8=action

    public static final String BLOCK_INSERT_SQLITE = """
            INSERT OR IGNORE INTO blocks(time, user, level, x, y, z, type, action)
            VALUES(?, (SELECT id FROM users WHERE uuid = ?), (SELECT id FROM levels WHERE name = ?), ?, ?, ?, (SELECT id FROM materials WHERE name = ?), ?)""";
    public static final String BLOCK_INSERT_MYSQL = """
            INSERT IGNORE INTO blocks(time, user, level, x, y, z, type, action)
            VALUES(?, (SELECT id FROM users WHERE uuid = ?), (SELECT id FROM levels WHERE name = ?), ?, ?, ?, (SELECT id FROM materials WHERE name = ?), ?)""";

    public static final String ENTITY_BLOCK_INSERT_SQLITE = """
            INSERT OR IGNORE INTO blocks(time, user, level, x, y, z, type, action)
            VALUES(?, (SELECT id FROM users WHERE uuid = ?), (SELECT id FROM levels WHERE name = ?), ?, ?, ?, (SELECT id FROM entities WHERE name = ?), ?)""";
    public static final String ENTITY_BLOCK_INSERT_MYSQL = """
            INSERT IGNORE INTO blocks(time, user, level, x, y, z, type, action)
            VALUES(?, (SELECT id FROM users WHERE uuid = ?), (SELECT id FROM levels WHERE name = ?), ?, ?, ?, (SELECT id FROM entities WHERE name = ?), ?)""";

    // --- normalized position lookups (dialect-neutral) ---
    // Params: 1=levelName, 2=x, 3=y, 4=z

    private static final String HISTORY_SELECT = """
            SELECT blocks.time, users.name, users.uuid, blocks.x, blocks.y, blocks.z, materials.name, blocks.action
            FROM blocks
            INNER JOIN users ON blocks.user = users.id
            INNER JOIN levels ON blocks.level = levels.id
            INNER JOIN materials ON blocks.type = materials.id
            WHERE levels.name = ? AND blocks.x = ? AND blocks.y = ? AND blocks.z = ?""";

    public static final String BLOCK_HISTORY_BY_POSITION_SQLITE =
            HISTORY_SELECT + " AND (blocks.action = 0 OR blocks.action = 1)\nORDER BY blocks.time DESC";
    public static final String BLOCK_HISTORY_BY_POSITION_MYSQL = BLOCK_HISTORY_BY_POSITION_SQLITE;

    public static final String INTERACTION_HISTORY_BY_POSITION_SQLITE =
            HISTORY_SELECT + " AND blocks.action = 2\nORDER BY blocks.time DESC";
    public static final String INTERACTION_HISTORY_BY_POSITION_MYSQL = INTERACTION_HISTORY_BY_POSITION_SQLITE;
}
