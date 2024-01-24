package com.daqem.grieflogger.database.repository;

import com.daqem.grieflogger.config.GriefLoggerCommonConfig;

public abstract class Repository implements IRepository {

    @Override
    public boolean isMysql() {
        return GriefLoggerCommonConfig.useMysql.get();
    }
}
