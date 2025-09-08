package com.daqem.grieflogger.config;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.yamlconfig.YamlConfigExpectPlatform;
import com.daqem.yamlconfig.api.config.ConfigExtension;
import com.daqem.yamlconfig.api.config.ConfigType;
import com.daqem.yamlconfig.api.config.entry.IConfigEntry;
import com.daqem.yamlconfig.impl.config.ConfigBuilder;

public class GriefLoggerConfig {

    public static void init() {
    }

    public static final IConfigEntry<Boolean> useMysql;
    public static final IConfigEntry<String> mysqlHost;
    public static final IConfigEntry<Integer> mysqlPort;
    public static final IConfigEntry<String> mysqlDatabase;
    public static final IConfigEntry<String> mysqlUsername;
    public static final IConfigEntry<String> mysqlPassword;
    public static final IConfigEntry<Integer> mysqlTimeout;
    public static final IConfigEntry<Boolean> useIndexes;

    public static final IConfigEntry<Integer> maxPageSize;

    public static final IConfigEntry<Boolean> serverSideOnlyMode;

    public static final IConfigEntry<Integer> queueFrequency;
    public static final IConfigEntry<Integer> helloFrequency;

    static {
        ConfigBuilder config = new ConfigBuilder(
                GriefLogger.MOD_ID,
                "grieflogger-server",
                ConfigExtension.YAML,
                ConfigType.SERVER,
                YamlConfigExpectPlatform.getConfigDirectory().resolve(GriefLogger.MOD_ID)
        );

        config.push("database");
        useMysql = config.defineBoolean("useMysql", false)
                .withComments("Whether to use MySQL or SQLite");
        mysqlHost = config.defineString("mysqlHost", "localhost", 1, 255)
                .withComments("MySQL host");
        mysqlPort = config.defineInteger("mysqlPort", 3306, 1, 65535)
                .withComments("MySQL port");
        mysqlDatabase = config.defineString("mysqlDatabase", "database", 1, 255)
                .withComments("MySQL database");
        mysqlUsername = config.defineString("mysqlUsername", "username", 1, 255)
                .withComments("MySQL username");
        mysqlPassword = config.defineString("mysqlPassword", "password", 1, 255)
                .withComments("MySQL password");
        mysqlTimeout = config.defineInteger("mysqlTimeout", 5000, 1, 60000)
                .withComments("MySQL timeout");
        useIndexes = config.defineBoolean("useIndexes", true)
                .withComments("Whether to use indexes (improves inspect/lookup speed)");
        config.pop();

        config.push("general");
        maxPageSize = config.defineInteger("maxPageSize", 10, 1, 100)
                .withComments("Maximum page size");
        config.pop();

        config.push("server");
        serverSideOnlyMode = config.defineBoolean("serverSideOnlyMode", true)
                .withComments("Whether to run the mod in server side only mode");
        config.pop();

        config.push("queue");
        queueFrequency = config.defineInteger("queueFrequency", 20, 1, 100)
                .withComments("The frequency at which the database queue is executed (every 'x' ticks)");
        config.pop();

        config.push("hello");
        helloFrequency = config.defineInteger("helloFrequency", 600, 1, 1000)
                .withComments("The frequency at which the hello packet is sent to the server (every 'x' ticks)");
        config.pop();

        config.build();
    }
}
