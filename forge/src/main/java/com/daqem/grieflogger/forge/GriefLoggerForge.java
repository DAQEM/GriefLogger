package com.daqem.grieflogger.forge;

import com.daqem.grieflogger.GriefLogger;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(GriefLogger.MOD_ID)
public class GriefLoggerForge {

    public GriefLoggerForge() {
        DistExecutor.safeRunForDist(
                () -> SideProxyForge.Client::new,
                () -> SideProxyForge.Server::new
        );
    }
}
