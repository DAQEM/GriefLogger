package com.daqem.grieflogger.database.queue;

import java.sql.PreparedStatement;

public interface IQueue {

    void add(PreparedStatement statement);

    /**
     * Enqueue an <em>event</em> insert (block/container/item) tagged for observability: if it affects
     * 0 rows at flush time it was silently dropped and is warned about, using {@code eventLabel} as
     * context. Dedup parent upserts use {@link #add(PreparedStatement)} (untagged) so their normal
     * 0-row "already exists" result is not warned.
     */
    void add(PreparedStatement statement, String eventLabel);

    void execute();
    void hello();
}
