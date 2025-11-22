package com.daqem.grieflogger.database.queue;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Queue implements IQueue {

    private final Database database;
    private final boolean isBatch;
    private final ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<>();

    public Queue(Database database, boolean isBatch) {
        this.database = database;
        this.isBatch = isBatch;
    }

    @Override
    public void add(SqlTask task) {
        this.queue.add(task);
    }

    @Override
    public void execute() {
        if (this.queue.isEmpty()) {
            return;
        }
        List<Object> items = new ArrayList<>();
        Object item;
        while ((item = this.queue.poll()) != null) {
            items.add(item);
        }
        this.database.executeQueue(items, isBatch);
    }

    @Override
    public void hello() {
        this.add(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
                statement.execute();
            } catch (Exception e) {
                GriefLogger.LOGGER.error("Failed to send hello packet", e);
            }
        });
    }
}