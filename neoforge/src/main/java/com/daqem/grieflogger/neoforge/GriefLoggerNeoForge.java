package com.daqem.grieflogger.neoforge;

import com.daqem.grieflogger.GriefLogger;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = GriefLogger.MOD_ID, dist = Dist.DEDICATED_SERVER)
public class GriefLoggerNeoForge {

    public GriefLoggerNeoForge() {
        GriefLogger.init();
    }
}
