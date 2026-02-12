package com.daqem.grieflogger;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.commands.CommandSourceStack;

public class GriefLoggerPermissions {

    /**
     * Checks permissions using the platform's specific API (LuckPerms/PermissionsAPI)
     * if available. Otherwise, falls back to the standard OP level check.
     *
     * @param source The command source.
     * @param permissionNode The string permission node (e.g., "grieflogger.command.inspect").
     * @param fallbackLevel The OP level to require if the permission API is missing (usually 2).
     * @return True if the source has permission.
     */
    @ExpectPlatform
    public static boolean check(CommandSourceStack source, String permissionNode, int fallbackLevel) {
        throw new AssertionError();
    }
}
