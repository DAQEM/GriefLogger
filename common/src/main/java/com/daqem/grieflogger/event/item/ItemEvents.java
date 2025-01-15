package com.daqem.grieflogger.event.item;

import dev.architectury.event.events.common.PlayerEvent;

public class ItemEvents {

    public static void registerEvents() {
        PlayerEvent.CRAFT_ITEM.register(CraftItemEvent::onCraftItem);
        PlayerEvent.DROP_ITEM.register(DropItemEvent::onDropItem);
        PlayerEvent.PICKUP_ITEM_POST.register(PickupItemEvent::onPickupItem);
        PlayerEvent.SMELT_ITEM.register(SmeltItemEvent::onSmeltItem);
    }
}
