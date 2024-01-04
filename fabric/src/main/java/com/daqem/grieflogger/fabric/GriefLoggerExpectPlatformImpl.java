package com.daqem.grieflogger.fabric;

import com.daqem.grieflogger.GriefLoggerExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class GriefLoggerExpectPlatformImpl {
    /**
     * This is our actual method to {@link GriefLoggerExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
