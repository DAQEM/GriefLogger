package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.config.GriefLoggerConfig;

public abstract class Repository implements IRepository {

    @Override
    public boolean isMysql() {
        return GriefLoggerConfig.useMysql.get();
    }
}
