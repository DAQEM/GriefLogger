package com.daqem.grieflogger.model.action;

import com.daqem.grieflogger.model.Operation;

public enum ItemAction implements Action {
    REMOVE(0, Operation.REMOVE), //DONE
    ADD(1, Operation.ADD), //DONE
    DROP(2, Operation.REMOVE), //DONE
    PICKUP(3, Operation.ADD), //DONE
    CRAFT(4, Operation.ADD), //DONE
    BREAK(5, Operation.REMOVE), //DONE
    CONSUME(6, Operation.REMOVE), //DONE
    THROW(7, Operation.REMOVE), //DONE
    SHOOT(8, Operation.REMOVE), //DONE
    ADD_ENDER_CHEST(9, Operation.ADD),
    REMOVE_ENDER_CHEST(10, Operation.REMOVE);

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
