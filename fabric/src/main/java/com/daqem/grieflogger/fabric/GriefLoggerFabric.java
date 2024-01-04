package com.daqem.grieflogger.fabric;

import com.daqem.grieflogger.GriefLogger;
import net.fabricmc.api.ModInitializer;

public class GriefLoggerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        GriefLogger.init();
    }
}
