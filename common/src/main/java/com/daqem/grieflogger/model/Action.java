package com.daqem.grieflogger.model;

import com.daqem.grieflogger.GriefLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum Action {
    BREAK(0, true),
    PLACE(1, false);

    private final int id;
    private final boolean isRemoval;

    Action(int id, boolean isRemoval) {
        this.id = id;
        this.isRemoval = isRemoval;
    }

    public int getId() {
        return id;
    }

    public static Action fromId(int id) {
        for (Action action : Action.values()) {
            if (action.getId() == id) {
                return action;
            }
        }
        return null;
    }

    public Component getPrefix() {
        return isRemoval ?
                GriefLogger.translate("action.prefix.removal").withStyle(ChatFormatting.RED) :
                GriefLogger.translate("action.prefix.placement").withStyle(ChatFormatting.GREEN);
    }

    public Component getPastTense() {
        return GriefLogger.translate("action." + this.toString().toLowerCase() + ".past");
    }
}
