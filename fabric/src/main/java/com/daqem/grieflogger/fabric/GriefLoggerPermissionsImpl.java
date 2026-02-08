package com.daqem.grieflogger.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permissions;

public class GriefLoggerPermissionsImpl {

    private static final boolean PERMISSIONS_API_LOADED = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

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