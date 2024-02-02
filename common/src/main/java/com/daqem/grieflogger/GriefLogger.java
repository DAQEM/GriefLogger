package com.daqem.grieflogger;

import com.daqem.grieflogger.config.GriefLoggerConfig;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.service.*;
import com.daqem.grieflogger.event.*;
import com.daqem.grieflogger.event.block.BlockEvents;
import com.daqem.grieflogger.event.item.ItemEvents;
import com.daqem.grieflogger.thread.ThreadManager;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class GriefLogger {
    public static final String MOD_ID = "grieflogger";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static Database DATABASE;

    public static void init() {
        initConfigs();
        registerEvents();
        prepareDatabaseAsync();
    }

    private static void initConfigs() {
        GriefLoggerConfig.init();
    }

    private static void registerEvents() {
        BlockEvents.registerEvents();
        TickEvents.registerEvents();
        EntityEvents.registerEvents();
        ItemEvents.registerEvents();

        PlayerJoinEvent.registerEvent();
        PlayerQuitEvent.registerEvent();
        LevelLoadEvent.registerEvent();
        RegisterCommandEvent.registerEvent();

        ChatEvent.registerEvent();
        CommandEvent.registerEvent();
    }

    private static void prepareDatabaseAsync() {
        ThreadManager.execute(() -> {
            LOGGER.info("Preparing database asynchronously.");
            long start = System.currentTimeMillis();
            prepareDatabase();
            long end = System.currentTimeMillis();
            LOGGER.info("Database prepared in {}ms.", end - start);
        });
    }

    private static void prepareDatabase() {
        DATABASE = new Database("database.db");

        Services.MATERIAL.createTableAsync();
        Services.USER.createTableAsync();
        Services.USERNAME.createTableAsync();
        Services.LEVEL.createTableAsync();
        Services.ENTITY.createTableAsync();
        Services.BLOCK.createTableAsync();
        Services.CONTAINER.createTableAsync();
        Services.SESSION.createTableAsync();
        Services.CHAT.createTableAsync();
        Services.COMMAND.createTableAsync();
        Services.ITEM.createTableAsync();
    }

    public static Database getDatabase() {
        return DATABASE;
    }

    public static MutableComponent translate(String str) {
        MutableComponent component = translate(str, TranslatableContents.NO_ARGS);
        if (GriefLoggerConfig.serverSideOnlyMode.get()) {
            component = Component.literal(component.getString()).withStyle(component.getStyle());
        }
        return component;
    }

    public static MutableComponent translate(String str, Object... args) {
        MutableComponent component = Component.translatable(MOD_ID + "." + str, args);
        if (GriefLoggerConfig.serverSideOnlyMode.get()) {
            component = Component.literal(component.getString()).withStyle(component.getStyle());
        }
        return component;
    }

    public static MutableComponent literal(String str) {
        return Component.literal(str);
    }

    public static MutableComponent themedTranslate(String str) {
        MutableComponent component = themedTranslate(str, TranslatableContents.NO_ARGS);
        if (GriefLoggerConfig.serverSideOnlyMode.get()) {
            component = Component.literal(component.getString()).withStyle(component.getStyle());
        }
        return component;
    }

    public static MutableComponent themedTranslate(String str, Object... args) {
        MutableComponent component = Component.translatable(MOD_ID + "." + str, args).withStyle(getTheme());
        if (GriefLoggerConfig.serverSideOnlyMode.get()) {
            component = Component.literal(component.getString()).withStyle(component.getStyle());
        }
        return component;
    }

    public static MutableComponent themedLiteral(String str) {
        MutableComponent component = Component.literal(str).withStyle(getTheme());
        if (GriefLoggerConfig.serverSideOnlyMode.get()) {
            component = Component.literal(component.getString()).withStyle(component.getStyle());
        }
        return component;
    }

    public static Component getName() {
        Component component = translate("name").withStyle(getTheme());
        if (GriefLoggerConfig.serverSideOnlyMode.get()) {
            component = Component.literal(component.getString()).withStyle(component.getStyle());
        }
        return component;
    }

    public static Style getTheme() {
        return Style.EMPTY.withColor(0xFCBA03);
    }

    public static ResourceLocation getId(String id) {
        return new ResourceLocation(MOD_ID, id);
    }
}
