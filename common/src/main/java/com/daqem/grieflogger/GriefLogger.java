package com.daqem.grieflogger;

import com.daqem.grieflogger.event.*;
import org.slf4j.Logger;

import com.daqem.grieflogger.config.GriefLoggerConfig;
import com.daqem.grieflogger.database.Database;
import com.daqem.grieflogger.database.service.Services;
import com.daqem.grieflogger.event.block.BlockEvents;
import com.daqem.grieflogger.event.item.ItemEvents;
import com.daqem.grieflogger.i18n.LanguageManager;
import com.mojang.logging.LogUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GriefLogger {
    public static final String MOD_ID = "grieflogger";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static Database DATABASE;

    public static void init() {
        initConfigs();
        boolean databaseReady = prepareDatabase();
        if (!databaseReady) {
            return;
        }
        registerEvents();
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
        ServerStartedEvent.registerEvent();
        ServerStoppedEvent.registerEvent();
    }

    private static boolean prepareDatabase() {
        LOGGER.info("Preparing GriefLogger database...");
        long start = System.currentTimeMillis();
        try {
            DATABASE = new Database();
            boolean connected = DATABASE.createConnection();
            if (!connected) {
                LOGGER.error("Failed to connect to database, disabling GriefLogger...");
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to connect to database, disabling GriefLogger...", e);
            return false;
        }

        Services.MATERIAL.createTable();
        Services.USER.createTable();
        Services.USERNAME.createTable();
        Services.LEVEL.createTable();
        Services.ENTITY.createTable();
        Services.BLOCK.createTable();
        Services.CONTAINER.createTable();
        Services.SESSION.createTable();
        Services.CHAT.createTable();
        Services.COMMAND.createTable();
        Services.ITEM.createTable();

        if (GriefLoggerConfig.useIndexes.get()) {
            Services.BLOCK.createIndexes();
            Services.CHAT.createIndexes();
            Services.COMMAND.createIndexes();
            Services.CONTAINER.createIndexes();
            Services.ITEM.createIndexes();
            Services.SESSION.createIndexes();
        }

        long end = System.currentTimeMillis();
        LOGGER.info("Database prepared in {}ms.", end - start);
        return true;
    }

    public static Database getDatabase() {
        return DATABASE;
    }

    public static MutableComponent translate(String str) {
        return translate(str, TranslatableContents.NO_ARGS);
    }

    public static MutableComponent translate(String str, Object... args) {
        String translated = LanguageManager.getString(MOD_ID + "." + str);
        try {
            if (args.length == 0) {
                return Component.literal(translated);
            }
            
            boolean hasComponentArg = false;
            for (Object arg : args) {
                if (arg instanceof Component) {
                    hasComponentArg = true;
                    break;
                }
            }

            if (!hasComponentArg) {
                return Component.literal(String.format(translated, args));
            }

            Object[] newArgs = new Object[args.length];
            Map<String, Component> componentMap = new HashMap<>();

            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Component component) {
                    String marker = "§_GL_COMP_" + i + "_§";
                    componentMap.put(marker, component);
                    newArgs[i] = marker;
                } else {
                    newArgs[i] = args[i];
                }
            }

            String formatted = String.format(translated, newArgs);
            MutableComponent root = Component.literal("");
            Matcher matcher = Pattern.compile("§_GL_COMP_(\\d+)_§").matcher(formatted);
            int lastEnd = 0;

            while (matcher.find()) {
                String textBefore = formatted.substring(lastEnd, matcher.start());
                if (!textBefore.isEmpty()) {
                    root.append(Component.literal(textBefore));
                }
                String marker = matcher.group();
                Component component = componentMap.get(marker);
                if (component != null) {
                    root.append(component);
                }
                lastEnd = matcher.end();
            }

            String textAfter = formatted.substring(lastEnd);
            if (!textAfter.isEmpty()) {
                root.append(Component.literal(textAfter));
            }

            return root;

        } catch (Exception e) {
            return Component.literal(translated);
        }
    }

    public static MutableComponent literal(String str) {
        return Component.literal(str);
    }

    public static MutableComponent themedTranslate(String str) {
        return themedTranslate(str, TranslatableContents.NO_ARGS);
    }

    public static MutableComponent themedTranslate(String str, Object... args) {
        return translate(str, args).withStyle(getTheme());
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

    public static Identifier getId(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }
}
