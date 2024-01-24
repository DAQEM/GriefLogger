package com.daqem.grieflogger.model.history;

import com.daqem.grieflogger.model.BlockPosition;
import com.daqem.grieflogger.model.Time;
import com.daqem.grieflogger.model.User;
import com.daqem.grieflogger.model.action.IAction;
import net.minecraft.network.chat.Component;

public interface IHistory {

    Time getTime();

    User getUser();

    BlockPosition getPosition();

    IAction getAction();

    Component getComponent();

    Component getMaterialComponent();

    Component getComponentWithPos();
}
