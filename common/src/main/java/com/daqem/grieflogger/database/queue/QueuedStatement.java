package com.daqem.grieflogger.database.queue;

import java.sql.PreparedStatement;

/**
 * A queued {@link PreparedStatement} paired with an optional event label for observability.
 *
 * <p>When {@code eventLabel} is non-null the statement is an <em>event</em> insert
 * (block/container/item) whose persistence matters: if it affects 0 rows at flush time it was
 * silently dropped (a referenced level/user/material row was missing), which is a bug worth warning
 * about. When {@code eventLabel} is null the statement is a dedup parent upsert
 * (materials/levels/users/entities) where affecting 0 rows simply means "already exists" — normal,
 * never warned. The label also gives the warning its context (which kind of event was lost).
 *
 * <p>Pure (no Minecraft types), so the warn decision is unit-testable without a runtime.
 */
public record QueuedStatement(PreparedStatement statement, String eventLabel) {

    /** A statement with no observability tagging (dedup parent upserts, batch statements). */
    public static QueuedStatement untagged(PreparedStatement statement) {
        return new QueuedStatement(statement, null);
    }

    /** Whether a 0-affected-rows result for this statement is a silent drop that should warn. */
    public boolean shouldWarnDropped(int affectedRows) {
        return eventLabel != null && affectedRows == 0;
    }
}
