package com.daqem.grieflogger.config;

import com.daqem.grieflogger.GriefLogger;
import com.mojang.text2speech.OperatingSystem;
import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.function.Supplier;

public class GriefLoggerConfig {

    public static void init() {
    }

    public static final Supplier<Boolean> useMysql;
    public static final Supplier<String> sqlDriver;
    public static final Supplier<String> mysqlHost;
    public static final Supplier<Integer> mysqlPort;
    public static final Supplier<String> mysqlDatabase;
    public static final Supplier<String> mysqlUsername;
    public static final Supplier<String> mysqlPassword;
    public static final Supplier<Integer> mysqlTimeout;
    public static final Supplier<Boolean> useIndexes;

    public static final Supplier<Integer> maxPageSize;

    public static final Supplier<Boolean> serverSideOnlyMode;

    public static final Supplier<Integer> queueFrequency;
    public static final Supplier<Integer> helloFrequency;

    static {
        IConfigBuilder config = ConfigBuilders.newTomlConfig(GriefLogger.MOD_ID, GriefLogger.MOD_ID, true);
        config.push("database");
        useMysql = config.comment("Whether to use MySQL/MariaDB or SQLite").onlyOnServer().define("useMysql", false);
        sqlDriver = config.comment("SQL driver to use when useMysql is true (mysql or mariadb)").onlyOnServer().define("sqlDriver", "mysql", 1, 10);
        mysqlHost = config.comment("MySQL/MariaDB host").onlyOnServer().define("mysqlHost", "localhost", 1, 255);
        mysqlPort = config.comment("MySQL/MariaDB port").onlyOnServer().define("mysqlPort", 3306, 1, 65535);
        mysqlDatabase = config.comment("MySQL/MariaDB database").onlyOnServer().define("mysqlDatabase", "database", 1, 255);
        mysqlUsername = config.comment("MySQL/MariaDB username").onlyOnServer().define("mysqlUsername", "username", 1, 255);
        mysqlPassword = config.comment("MySQL/MariaDB password").onlyOnServer().define("mysqlPassword", "password", 1, 255);
        mysqlTimeout = config.comment("MySQL/MariaDB timeout").onlyOnServer().define("mysqlTimeout", 5000, 1, 60000);
        useIndexes = config.comment("Whether to use indexes (improves inspect/lookup speed)").onlyOnServer().define("useIndexes", true);
        config.pop();

        config.push("general");
        maxPageSize = config.comment("Maximum page size").onlyOnServer().define("maxPageSize", 10, 1, 100);
        config.pop();

        config.push("server");
        serverSideOnlyMode = config.comment("Whether to run the mod in server side only mode").onlyOnServer().define("serverSideOnlyMode", true);
        config.pop();

        config.push("queue");
        queueFrequency = config.comment("The frequency at which the database queue is executed (every 'x' ticks)").onlyOnServer().define("queueFrequency", 20, 1, 100);
        config.pop();

        config.push("hello");
        helloFrequency = config.comment("The frequency at which the hello packet is sent to the server (every 'x' ticks)").onlyOnServer().define("helloFrequency", 600, 1, 1000);
        config.pop();

        config.build();
    }
}
