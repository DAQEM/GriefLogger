package com.daqem.grieflogger.model.history;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.model.BlockPosition;
import com.daqem.grieflogger.model.SimpleItemStack;
import com.daqem.grieflogger.model.Time;
import com.daqem.grieflogger.model.User;
import com.daqem.grieflogger.model.action.IAction;
import com.daqem.grieflogger.model.action.ItemAction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class ContainerHistory extends ItemHistory {

    public ContainerHistory(long time, String name, String uuid, int x, int y, int z, String material, DataComponentPatch data, int amount, int action) {
        super(time, name, uuid, x, y, z, material, data, amount, action);
    }

    public ContainerHistory(Time time, User user, BlockPosition position, SimpleItemStack itemStack, IAction action) {
        super(time, user, position, itemStack, action);
    }
}
