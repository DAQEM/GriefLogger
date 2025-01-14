package com.daqem.grieflogger.neoforge;

import com.daqem.grieflogger.GriefLogger;

public class SideProxyNeoForge {

    SideProxyNeoForge() {
    }

    public static class Server extends SideProxyNeoForge {
        Server() {
            GriefLogger.init();
        }
    }

    public static class Client extends SideProxyNeoForge {

        Client() {
        }
    }
}
