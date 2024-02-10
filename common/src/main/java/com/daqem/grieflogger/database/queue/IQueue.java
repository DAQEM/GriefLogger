package com.daqem.grieflogger.database.queue;

import java.sql.PreparedStatement;

public interface IQueue {

    void add(PreparedStatement statement);
    void execute();
}
