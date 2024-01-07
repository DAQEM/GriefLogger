package com.daqem.grieflogger;

import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.service.*;
import com.daqem.grieflogger.event.LevelLoadEvent;
import com.daqem.grieflogger.event.BlockEvents;
import com.daqem.grieflogger.event.PlayerJoinEvent;
import com.daqem.grieflogger.event.RegisterCommandEvent;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
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
        RegisterCommandEvent.registerEvent();
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

    public static MutableComponent translate(String str) {
        return translate(str, TranslatableContents.NO_ARGS);
    }

    public static MutableComponent translate(String str, Object... args) {
        return Component.translatable(MOD_ID + "." + str, args);
    }

    public static MutableComponent literal(String str) {
        return Component.literal(str);
    }

    public static MutableComponent themedTranslate(String str) {
        return themedTranslate(str, TranslatableContents.NO_ARGS);
    }

    public static MutableComponent themedTranslate(String str, Object... args) {
        return Component.translatable(MOD_ID + "." + str, args).withStyle(getTheme());
    }

    public static MutableComponent themedLiteral(String str) {
        return Component.literal(str).withStyle(getTheme());
    }

    public static Component getName() {
        return translate("name").withStyle(getTheme());
    }

    public static Style getTheme() {
        return Style.EMPTY.withColor(0xFCBA03);
    }
}
