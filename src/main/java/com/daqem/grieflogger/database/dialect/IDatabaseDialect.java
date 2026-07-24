package com.daqem.grieflogger.database.dialect;

public interface IDatabaseDialect {

    String getInsertIgnore();

    String getOnConflictUpdate(String key, String update);

    String getOnConflictDoNothing(String key);

    String getDataType(String type);
}
