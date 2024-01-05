package com.daqem.grieflogger;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.service.*;
import com.daqem.grieflogger.event.LevelLoadEvent;
import com.daqem.grieflogger.event.BlockEvents;
import com.daqem.grieflogger.event.PlayerJoinEvent;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class GriefLogger {
    public static final String MOD_ID = "grieflogger";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static Database DATABASE;

    public static void init() {
        registerEvents();
        prepareDatabase();
    }

    private static void registerEvents() {
        PlayerJoinEvent.registerEvent();
        LevelLoadEvent.registerEvent();
        BlockEvents.registerEvents();
    }

    private static void prepareDatabase() {
        DATABASE = new Database("database.db");

        new MaterialService(getDatabase()).createTable();
        new UserService(getDatabase()).createTable();
        new UsernameService(getDatabase()).createTable();
        new BlockService(getDatabase()).createTable();
        new LevelService(getDatabase()).createTable();
    }

    public static Database getDatabase() {
        return DATABASE;
    }
}
