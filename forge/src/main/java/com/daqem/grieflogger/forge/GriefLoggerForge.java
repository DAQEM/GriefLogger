package com.daqem.grieflogger.forge;

import com.daqem.grieflogger.GriefLogger;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GriefLogger.MOD_ID)
public class GriefLoggerForge {
    public GriefLoggerForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(GriefLogger.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        GriefLogger.init();
    }
}
