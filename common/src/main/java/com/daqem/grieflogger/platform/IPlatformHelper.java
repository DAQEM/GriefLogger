package com.daqem.grieflogger.platform;

import net.minecraft.commands.CommandSourceStack;

import java.nio.file.Path;

public interface IPlatformHelper {

    Path getConfigDirectory();

    /**
     * Checks permissions using the platform's specific permission API (LuckPerms/PermissionsAPI)
     * if available. Otherwise, falls back to the standard OP level check.
     *
     * @param source The command source.
     * @param permissionNode The string permission node (e.g., "grieflogger.command.inspect").
     * @param fallbackLevel The OP level to require if the permission API is missing (usually 2).
     * @return True if the source has permission.
     */
    boolean checkPermission(CommandSourceStack source, String permissionNode, int fallbackLevel);
}
