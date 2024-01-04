package com.daqem.grieflogger.forge;

import com.daqem.grieflogger.GriefLoggerExpectPlatform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class GriefLoggerExpectPlatformImpl {
    /**
     * This is our actual method to {@link GriefLoggerExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
