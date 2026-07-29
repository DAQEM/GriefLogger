package com.daqem.grieflogger.event.block;

import com.daqem.knot.events.EventsService;

public class BlockEvents {

    public static void registerEvents() {
        EventsService.Block.BREAK_BLOCK.register(BreakBlockEvent::breakBlock);
        EventsService.Block.PLACE_BLOCK.register(PlaceBlockEvent::placeBlock);
        EventsService.Block.LEFT_CLICK_BLOCK.register(LeftClickBlockEvent::leftClickBlock);
        EventsService.Block.RIGHT_CLICK_BLOCK.register(RightClickBlockEvent::rightClickBlock);
    }
}
