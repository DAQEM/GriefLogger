package com.daqem.grieflogger.event.block;

import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.InteractionEvent;

public class BlockEvents {

    public static void registerEvents() {
        BlockEvent.BREAK.register(BreakBlockEvent::breakBlock);
        BlockEvent.PLACE.register(PlaceBlockEvent::placeBlock);
        InteractionEvent.LEFT_CLICK_BLOCK.register(LeftClickBlockEvent::leftClickBlock);
        InteractionEvent.RIGHT_CLICK_BLOCK.register(RightClickBlockEvent::rightClickBlock);
    }
}
