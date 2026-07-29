package com.daqem.grieflogger.event.item;

import com.daqem.knot.events.EventsService;

public class ItemEvents {

    public static void registerEvents() {
        EventsService.Item.CRAFT_ITEM.register(CraftItemEvent::onCraftItem);
        EventsService.Item.PICKUP_ITEM.register(PickupItemEvent::onPickupItem);
        EventsService.Player.SMELT_ITEM.register(SmeltItemEvent::onSmeltItem);
    }
}
