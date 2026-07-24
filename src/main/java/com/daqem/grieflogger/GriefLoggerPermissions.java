package com.daqem.grieflogger;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class GriefLoggerPermissions {

    private static final boolean PERMISSIONS_API_LOADED = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    /**
     * Checks permissions using the fabric-permissions-api (LuckPerms/PermissionsAPI)
     * if available. Otherwise, falls back to the standard OP level check.
     *
     * @param source The command source.
     * @param permissionNode The string permission node (e.g., "grieflogger.command.inspect").
     * @param fallbackLevel The OP level to require if the permission API is missing (usually 2).
     * @return True if the source has permission.
     */
    public static boolean check(CommandSourceStack source, String permissionNode, int fallbackLevel) {
        if (PERMISSIONS_API_LOADED) {
            try {
                return me.lucko.fabric.api.permissions.v0.Permissions.check(source, permissionNode, fallbackLevel);
            } catch (Throwable t) {
                // Fallback if something goes wrong with the API
                return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(fallbackLevel)));
            }
        }
        return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(fallbackLevel)));
    }
}
