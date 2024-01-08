package com.daqem.grieflogger.model.action;

public enum SessionAction {
    JOIN(0),
    QUIT(1);

    private final int id;

    SessionAction(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SessionAction fromId(int id) {
        for (SessionAction sessionAction : SessionAction.values()) {
            if (sessionAction.getId() == id) {
                return sessionAction;
            }
        }
        return null;
    }
}
