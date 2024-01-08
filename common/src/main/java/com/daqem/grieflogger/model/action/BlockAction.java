package com.daqem.grieflogger.model.action;

import com.daqem.grieflogger.model.Operation;

public enum BlockAction implements Action {
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

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    public static BlockAction fromId(int id) {
        for (BlockAction blockAction : BlockAction.values()) {
            if (blockAction.getId() == id) {
                return blockAction;
            }
        }
        return null;
    }
}
