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
    public static final Supplier<String> mysqlHost;
    public static final Supplier<Integer> mysqlPort;
    public static final Supplier<String> mysqlDatabase;
    public static final Supplier<String> mysqlUsername;
    public static final Supplier<String> mysqlPassword;
    public static final Supplier<Integer> mysqlTimeout;
    public static final Supplier<Boolean> useIndexes;

    public static final Supplier<Integer> maxPageSize;

    public enum Language { en_us, nl_nl, zh_tw }
    public static final Supplier<Language> language;

    public static final Supplier<Integer> queueFrequency;
    public static final Supplier<Integer> helloFrequency;

    static {
        IConfigBuilder config = ConfigBuilders.newTomlConfig(GriefLogger.MOD_ID, GriefLogger.MOD_ID, true);
        config.push("database");
        useMysql = config.comment("Whether to use MySQL or SQLite").onlyOnServer().define("useMysql", false);
        mysqlHost = config.comment("MySQL host").onlyOnServer().define("mysqlHost", "localhost", 1, 255);
        mysqlPort = config.comment("MySQL port").onlyOnServer().define("mysqlPort", 3306, 1, 65535);
        mysqlDatabase = config.comment("MySQL database").onlyOnServer().define("mysqlDatabase", "database", 1, 255);
        mysqlUsername = config.comment("MySQL username").onlyOnServer().define("mysqlUsername", "username", 1, 255);
        mysqlPassword = config.comment("MySQL password").onlyOnServer().define("mysqlPassword", "password", 1, 255);
        mysqlTimeout = config.comment("MySQL timeout").onlyOnServer().define("mysqlTimeout", 5000, 1, 60000);
        useIndexes = config.comment("Whether to use indexes (improves inspect/lookup speed)").onlyOnServer().define("useIndexes", true);
        config.pop();

        config.push("general");
        maxPageSize = config.comment("Maximum page size").onlyOnServer().define("maxPageSize", 10, 1, 100);
        config.pop();

        config.push("server");
        language = config.comment("The language to use for translations (en_us, nl_nl, zh_tw)").onlyOnServer().define("language", Language.en_us);
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
