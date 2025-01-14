package com.daqem.grieflogger.neoforge;

import com.daqem.grieflogger.GriefLogger;
import dev.architectury.utils.EnvExecutor;
import net.neoforged.fml.common.Mod;

@Mod(GriefLogger.MOD_ID)
public class GriefLoggerNeoForge {

    public GriefLoggerNeoForge() {
        EnvExecutor.getEnvSpecific(
                () -> SideProxyNeoForge.Client::new,
                () -> SideProxyNeoForge.Server::new
        );
    }
}
