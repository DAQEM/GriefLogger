package com.daqem.grieflogger.model;

public enum Action {
    BREAK(0),
    PLACE(1);

    private final int id;

    Action(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
