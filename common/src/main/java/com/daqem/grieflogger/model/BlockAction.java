package com.daqem.grieflogger.model;

import com.daqem.grieflogger.GriefLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum BlockAction {
    BREAK(0, Operation.REMOVE),
    PLACE(1, Operation.ADD),
    INTERACT(2, Operation.NEUTRAL),
    KILL(3, Operation.REMOVE);

    private final int id;
    private final Operation operation;

    BlockAction(int id, Operation operation) {
        this.id = id;
        this.operation = operation;
    }

    public int getId() {
        return id;
    }

    public static BlockAction fromId(int id) {
        for (BlockAction blockAction : BlockAction.values()) {
            if (blockAction.getId() == id) {
                return blockAction;
            }
        }
        return null;
    }

    public Component getPrefix() {
        return switch (operation) {
            case ADD -> GriefLogger.translate("action.prefix.add").withStyle(ChatFormatting.GREEN);
            case REMOVE -> GriefLogger.translate("action.prefix.remove").withStyle(ChatFormatting.RED);
            case NEUTRAL -> GriefLogger.translate("action.prefix.neutral");
        };
    }

    public Component getPastTense() {
        return GriefLogger.translate("action." + this.toString().toLowerCase() + ".past");
    }
}
