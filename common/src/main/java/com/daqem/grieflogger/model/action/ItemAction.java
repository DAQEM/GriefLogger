package com.daqem.grieflogger.model.action;

import com.daqem.grieflogger.model.Operation;

public enum ItemAction implements Action {
    REMOVE(0, Operation.REMOVE),
    ADD(1, Operation.ADD),
    DROP(2, Operation.REMOVE),
    PICKUP(3, Operation.ADD);

    private final int id;
    private final Operation operation;

    ItemAction(int id, Operation operation) {
        this.id = id;
        this.operation = operation;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    public static ItemAction fromId(int id) {
        for (ItemAction itemAction : ItemAction.values()) {
            if (itemAction.getId() == id) {
                return itemAction;
            }
        }
        return null;
    }
}
