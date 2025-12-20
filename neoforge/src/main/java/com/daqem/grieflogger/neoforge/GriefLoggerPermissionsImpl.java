package com.daqem.grieflogger.neoforge;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GriefLoggerPermissionsImpl {

    // Cache dynamic nodes so we don't re-create them constantly,
    // though ideally, nodes should be registered during startup event.
    // For simple compatibility without heavy registration logic, checking string nodes
    // often requires the permission manager (LuckPerms) to handle unregistered nodes gracefully.
    private static final Map<String, PermissionNode<Boolean>> NODES = new ConcurrentHashMap<>();

    public static boolean check(CommandSourceStack source, String permissionNode, int fallbackLevel) {
        if (source.getEntity() instanceof ServerPlayer player) {
            // LuckPerms on NeoForge can usually intercept permission checks even if
            // the node isn't strictly registered in the PermissionAPI registry,
            // depending on how the PermissionHandler is implemented.
            // However, the "Correct" way is to use PermissionAPI.getPermission.

            // If you want strict node registration, you'd need a registry event.
            // For lightweight compat, we try to get the permission value.

            // Create a temporary node wrapper or lookup existing (Logic depends on if you want
            // to pre-register specific nodes or allow dynamic strings).

            PermissionNode<Boolean> node = NODES.computeIfAbsent(permissionNode, id ->
                    new PermissionNode<>(
                            "grieflogger",
                            id.replace("grieflogger.", ""),
                            PermissionTypes.BOOLEAN,
                            (p, uuid, context) -> false
                    )
            );

            // Note: Using PermissionAPI with unregistered nodes might warn or default to false
            // depending on the implementation installed (e.g. Default vs LuckPerms).
            // LuckPerms usually handles unregistered lookups fine.
            try {
                return PermissionAPI.getPermission(player, node);
            } catch (Exception e) {
                // Fallback to OP if PermissionAPI fails or node is unknown/unregistered context
                return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(fallbackLevel)));
            }
        }

        // Console / Command Blocks
        return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(fallbackLevel)));
    }
}