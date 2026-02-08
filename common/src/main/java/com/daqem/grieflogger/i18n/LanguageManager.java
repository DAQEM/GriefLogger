package com.daqem.grieflogger.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.config.GriefLoggerConfig;

import com.daqem.yamlconfig.YamlConfigExpectPlatform;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.architectury.platform.Platform;
import net.minecraft.locale.Language;
import net.minecraft.server.MinecraftServer;

public class LanguageManager {

    private static final Map<String, String> TRANSLATIONS = new HashMap<>();
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static void load(MinecraftServer server) {
        TRANSLATIONS.clear();
        String language = GriefLoggerConfig.language.get();

        for (String modId : Platform.getModIds()) {
            if (!language.equals("en_us")) {
                if (modId.equals("minecraft")) {
                    loadLanguage(server, language);
                } else {
                    loadLanguage(modId, language);
                }
            }
            loadLanguage(modId, "en_us");
        }

        GriefLogger.LOGGER.info("Loaded {} translations for language: {}", TRANSLATIONS.size(), language);
    }

    private static void loadLanguage(String modId, String language) {
        String location = "/assets/" + modId + "/lang/" + language + ".json";
        try (InputStream stream = getInputStream(location)) {
            if (stream == null) {
                return;
            }
            Language.loadFromJson(stream, TRANSLATIONS::putIfAbsent);
        } catch (Exception e) {
            GriefLogger.LOGGER.error("Failed to load language file for mod: {}, language: {}", modId, language, e);
        }
    }

    private static InputStream getInputStream(String location) {
        if (Platform.isFabric()) {
            return Language.class.getResourceAsStream(location);
        }
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
    }

    private static void loadLanguage(MinecraftServer server, String languageCode) {
        try {
            String mcVersion = server.getServerVersion();

            Path cacheDir = YamlConfigExpectPlatform.getConfigDirectory().resolve(GriefLogger.MOD_ID).resolve("lang_cache");
            Files.createDirectories(cacheDir);

            Path cacheFile = cacheDir.resolve(languageCode + ".json");

            // 1. Get asset index URL for current version
            String assetIndexUrl = getAssetIndexUrl(mcVersion);

            // 2. Get hash for the specific lang file
            String hash = getLanguageHash(assetIndexUrl, "minecraft/lang/" + languageCode + ".json");
            String prefix = hash.substring(0, 2);
            String assetUrl = "https://resources.download.minecraft.net/" + prefix + "/" + hash;

            // 3. Download if missing or hash mismatch
            if (Files.notExists(cacheFile) || !fileHash(cacheFile).equalsIgnoreCase(hash)) {
                downloadFile(assetUrl, cacheFile);
            }

            // 4. Parse and cache
            String json = Files.readString(cacheFile);
            Map<String, String> translations = GSON.fromJson(json, TypeToken.getParameterized(Map.class, String.class, String.class).getType());

            TRANSLATIONS.putAll(translations);
        } catch (Exception e) {
            GriefLogger.LOGGER.error("Failed to download Minecraft language file for language: {}", languageCode, e);
        }
    }

    private static String getAssetIndexUrl(String mcVersion) throws Exception {
        String manifestJson = get("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json");

        JsonObject manifest = GSON.fromJson(manifestJson, JsonObject.class);
        String versionUrl = manifest.getAsJsonArray("versions").asList().stream()
                .map(JsonElement::getAsJsonObject)
                .filter(v -> mcVersion.equals(v.get("id").getAsString()))
                .findFirst()
                .orElseThrow(() -> new IOException("Version " + mcVersion + " not found in manifest"))
                .get("url").getAsString();

        JsonObject versionJson = GSON.fromJson(get(versionUrl), JsonObject.class);
        return versionJson.getAsJsonObject("assetIndex").get("url").getAsString();
    }

    private static String getLanguageHash(String assetIndexUrl, String assetPath) throws Exception {
        JsonObject index = GSON.fromJson(get(assetIndexUrl), JsonObject.class);
        JsonObject objects = index.getAsJsonObject("objects");

        if (!objects.has(assetPath)) {
            throw new IOException("Language file not found in asset index: " + assetPath);
        }

        return objects.getAsJsonObject(assetPath)
                .get("hash").getAsString();
    }

    private static String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("User-Agent", "Minecraft-Server-Mod/1.0 (Java 21)")
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }
        return response.body();
    }

    private static void downloadFile(String url, Path destination) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("User-Agent", "Minecraft-Server-Mod/1.0")
                .build();

        HttpResponse<Path> response = HTTP_CLIENT.send(request,
                HttpResponse.BodyHandlers.ofFile(destination));

        if (response.statusCode() >= 400) {
            throw new IOException("Failed to download " + url + " (HTTP " + response.statusCode() + ")");
        }
    }

    private static String fileHash(Path file) throws IOException {
        byte[] data = Files.readAllBytes(file);
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return HexFormat.of().formatHex(sha1.digest(data)).toLowerCase();
    }

    public static String getString(String key) {
        return TRANSLATIONS.getOrDefault(key, key);
    }
}
