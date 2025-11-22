package com.daqem.grieflogger.i18n;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.config.GriefLoggerConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;

public class LanguageManager {

    private static final Map<String, String> TRANSLATIONS = new HashMap<>();
    private static final Gson GSON = new Gson();

    public static void load() {
        TRANSLATIONS.clear();
        String language = GriefLoggerConfig.language.get();

        for (Mod mod : Platform.getMods()) {
            String modId = mod.getModId();
            loadFromMod(modId, language);
            if (!language.equals("en_us")) {
                loadFromMod(modId, "en_us");
            }
        }
    }

    private static void loadFromMod(String modId, String language) {
        String pathStr = "assets/" + modId + "/lang/" + language + ".json";
        try (InputStream inputStream = GriefLogger.class.getClassLoader().getResourceAsStream(pathStr)) {
            if (inputStream != null) {
                try {
                    JsonObject jsonObject = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
                    jsonObject.entrySet().forEach(entry -> {
                        if (language.equals("en_us")) {
                            TRANSLATIONS.putIfAbsent(entry.getKey(), entry.getValue().getAsString());
                        } else {
                            TRANSLATIONS.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    });
                } catch (Exception e) {
                    GriefLogger.LOGGER.error("Failed to parse language file: {}", pathStr, e);
                }
            }
        } catch (Exception e) {
            // Ignore if file not found, it's expected for many mods
        }
    }

    public static String getString(String key) {
        return TRANSLATIONS.getOrDefault(key, key);
    }
}
