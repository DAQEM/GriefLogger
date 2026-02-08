package org.sqlite.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A fake implementation of ProcessRunner to satisfy CurseForge malware scans.
 * This removes OS interaction via Runtime.exec(), forcing SQLite to rely on
 * System.getProperty("os.name") and ("os.arch"), which is enough for Minecraft.
 */
public class ProcessRunner {

    public ProcessRunner() {
    }

    String runAndWaitFor(String command) throws IOException, InterruptedException {
        // Return empty string to force OSInfo to fall back to System properties
        return "";
    }

    String runAndWaitFor(String command, long timeout, TimeUnit unit)
            throws IOException, InterruptedException {
        // Return empty string to force OSInfo to fall back to System properties
        return "";
    }
}