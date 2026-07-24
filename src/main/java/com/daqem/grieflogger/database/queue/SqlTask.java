package com.daqem.grieflogger.database.queue;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlTask {
    void execute(Connection connection) throws SQLException;
}
