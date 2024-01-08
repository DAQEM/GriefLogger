package com.daqem.grieflogger.model.action;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.model.Operation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public interface Action {

    int getId();
    Operation getOperation();

    default Component getPrefix() {
        return switch (getOperation()) {
            case ADD -> GriefLogger.translate("action.prefix.add").withStyle(ChatFormatting.GREEN);
            case REMOVE -> GriefLogger.translate("action.prefix.remove").withStyle(ChatFormatting.RED);
            case NEUTRAL -> GriefLogger.translate("action.prefix.neutral");
        };
    }

    default Component getPastTense() {
        return GriefLogger.translate("action." + this.toString().toLowerCase() + ".past");
    }
}
