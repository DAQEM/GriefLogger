package com.daqem.grieflogger.database.queue;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.database.Database;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class Queue implements IQueue {

    private final Database database;
    private final boolean isBatch;
    private final List<PreparedStatement> statements = new ArrayList<>();

    public Queue(Database database, boolean isBatch) {
        this.database = database;
        this.isBatch = isBatch;
    }

    @Override
    public void add(PreparedStatement statement) {
        this.statements.add(statement);
    }

    @Override
    public void execute() {
        if (this.statements.isEmpty()) {
            return;
        }
        List<PreparedStatement> statements = new ArrayList<>(this.statements);
        this.statements.clear();
        this.database.executeStatements(statements, isBatch);
    }

    @Override
    public void hello() {
        try {
            PreparedStatement statement = this.database.prepareStatement("SELECT 1");
            this.database.execute(statement.toString(), false);
        } catch (Exception e) {
            GriefLogger.LOGGER.error("Failed to send hello packet", e);
        }
    }
}
