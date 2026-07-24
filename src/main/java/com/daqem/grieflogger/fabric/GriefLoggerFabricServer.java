package com.daqem.grieflogger.fabric;

import com.daqem.grieflogger.GriefLogger;
import net.fabricmc.api.DedicatedServerModInitializer;

public class GriefLoggerFabricServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        GriefLogger.init();
    }
}
