package com.daqem.grieflogger.database.queue;

public interface IQueue {

    void add(SqlTask task);
    void execute();
    void hello();
    boolean isEmpty();
}