package com.daqem.grieflogger.config;

import com.daqem.grieflogger.GriefLogger;
import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.function.Supplier;

public class GriefLoggerConfig {

    public static void init() {
    }

    public static final Supplier<Boolean> useMysql;
    public static final Supplier<String> mysqlHost;
    public static final Supplier<Integer> mysqlPort;
    public static final Supplier<String> mysqlDatabase;
    public static final Supplier<String> mysqlUsername;
    public static final Supplier<String> mysqlPassword;

    public static final Supplier<Integer> maxPageSize;

    public static final Supplier<Boolean> serverSideOnlyMode;

    static {
        IConfigBuilder config = ConfigBuilders.newTomlConfig(GriefLogger.MOD_ID, GriefLogger.MOD_ID, true).onlyOnServer();
        config.push("database");
        useMysql = config.comment("Whether to use MySQL or SQLite").define("useMysql", false);
        mysqlHost = config.comment("MySQL host").define("mysqlHost", "localhost", 1, 255);
        mysqlPort = config.comment("MySQL port").define("mysqlPort", 3306, 1, 65535);
        mysqlDatabase = config.comment("MySQL database").define("mysqlDatabase", "database", 1, 255);
        mysqlUsername = config.comment("MySQL username").define("mysqlUsername", "username", 1, 255);
        mysqlPassword = config.comment("MySQL password").define("mysqlPassword", "password", 1, 255);
        config.pop();

        config.push("general");
        maxPageSize = config.comment("Maximum page size").define("maxPageSize", 10, 1, 100);
        config.pop();

        config.push("server");
        serverSideOnlyMode = config.comment("Whether to run the mod in server side only mode").define("serverSideOnlyMode", true);
        config.pop();
        config.build();
    }

}
