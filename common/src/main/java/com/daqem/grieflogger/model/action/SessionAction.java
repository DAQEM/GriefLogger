package com.daqem.grieflogger.model.action;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.model.Operation;
import net.minecraft.network.chat.Component;

public enum SessionAction implements IAction {
    JOIN(0, Operation.ADD),
    QUIT(1, Operation.REMOVE);

    private final int id;
    private final Operation operation;

    SessionAction(int id, Operation operation) {
        this.id = id;
        this.operation = operation;
    }

    public int getId() {
        return id;
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    public static SessionAction fromId(int id) {
        for (SessionAction sessionAction : SessionAction.values()) {
            if (sessionAction.getId() == id) {
                return sessionAction;
            }
        }
        return null;
    }

    @Override
    public Component getPastTense() {
        return GriefLogger.translate("action." + this.name().toLowerCase() + ".past");
    }
}
