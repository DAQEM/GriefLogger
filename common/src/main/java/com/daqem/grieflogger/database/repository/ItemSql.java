package com.daqem.grieflogger.database.repository;

/**
 * Centralized SQL for item (drop/pickup/inventory) event persistence.
 *
 * <p>Kept free of any Minecraft/mod types so the exact statements the repository emits can be
 * exercised directly against a database in tests. Two dialect variants are provided where they
 * differ: SQLite (default backend) uses {@code INSERT OR IGNORE}; MySQL uses {@code INSERT IGNORE}.
 *
 * <p>Reliability (GAP E): the item insert is a plain {@code INSERT} that resolves its
 * {@code NOT NULL} foreign keys ({@code level}/{@code user}/{@code type}) via scalar sub-selects.
 * A missing parent row makes a sub-select resolve to {@code NULL}, which violates the column and
 * throws — aborting the whole flush batch's commit. Callers MUST upsert the parent rows (level,
 * user, material) before the item insert so the sub-selects resolve. The item inspect query
 * ({@code ItemRepository.getFilteredItemHistory}) already uses a normalized
 * {@code JOIN levels ON items.level = levels.id WHERE levels.name = ?}, so no read change is needed.
 */
public final class ItemSql {

    private ItemSql() {
    }

    // --- parent upserts (idempotent; run before the item insert) ---

    public static final String MATERIAL_UPSERT_SQLITE = "INSERT OR IGNORE INTO materials(name) VALUES(?)";
    public static final String MATERIAL_UPSERT_MYSQL = "INSERT IGNORE INTO materials(name) VALUES(?)";

    public static final String LEVEL_UPSERT_SQLITE = "INSERT OR IGNORE INTO levels(name) VALUES(?)";
    public static final String LEVEL_UPSERT_MYSQL = "INSERT IGNORE INTO levels(name) VALUES(?)";

    // --- item insert (resolves FKs via sub-select; parents must already be upserted) ---
    // Params: 1=time, 2=uuid, 3=levelName, 4=x, 5=y, 6=z, 7=material, 8=data, 9=amount, 10=action

    public static final String ITEM_INSERT_SQLITE = """
            INSERT INTO items(time, user, level, x, y, z, type, data, amount, action)
            VALUES(?, (SELECT id FROM users WHERE uuid = ?), (SELECT id FROM levels WHERE name = ?), ?, ?, ?, (SELECT id FROM materials WHERE name = ?), ?, ?, ?)""";
    public static final String ITEM_INSERT_MYSQL = ITEM_INSERT_SQLITE;
}
