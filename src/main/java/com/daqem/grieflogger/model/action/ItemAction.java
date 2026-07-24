package com.daqem.grieflogger.model.action;

import com.daqem.grieflogger.model.Operation;

public enum ItemAction implements IAction {
    REMOVE_ITEM(0, Operation.REMOVE),
    ADD_ITEM(1, Operation.ADD),
    DROP_ITEM(2, Operation.REMOVE),
    PICKUP_ITEM(3, Operation.ADD),
    CRAFT_ITEM(4, Operation.ADD),
    BREAK_ITEM(5, Operation.REMOVE),
    CONSUME_ITEM(6, Operation.REMOVE),
    THROW_ITEM(7, Operation.REMOVE),
    SHOOT_ITEM(8, Operation.REMOVE),
    ADD_ITEM_ENDER(9, Operation.ADD),
    REMOVE_ITEM_ENDER(10, Operation.REMOVE);

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
