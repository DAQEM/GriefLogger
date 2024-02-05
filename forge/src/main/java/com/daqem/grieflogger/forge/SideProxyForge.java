package com.daqem.grieflogger.forge;

import com.daqem.grieflogger.GriefLogger;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SideProxyForge {

    SideProxyForge() {
        EventBuses.registerModEventBus(GriefLogger.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static class Server extends SideProxyForge {
        Server() {
            GriefLogger.init();
        }
    }

    public static class Client extends SideProxyForge {

        Client() {
        }
    }
}
