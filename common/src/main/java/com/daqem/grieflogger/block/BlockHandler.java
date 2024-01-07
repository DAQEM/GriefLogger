package com.daqem.grieflogger.block;

import net.minecraft.world.level.block.*;

import java.util.List;

public class BlockHandler {

    public static boolean isBlockIntractable(Block block) {
        if (block instanceof FenceGateBlock
                || block instanceof DispenserBlock
                || block instanceof NoteBlock
                || block instanceof AbstractChestBlock
                || block instanceof AbstractFurnaceBlock
                || block instanceof LeverBlock
                || block instanceof TrapDoorBlock
                || block instanceof DoorBlock
                || block instanceof BrewingStandBlock
                || block instanceof DiodeBlock
                || block instanceof HopperBlock
                || block instanceof DropperBlock
                || block instanceof ShulkerBoxBlock
                || block instanceof BarrelBlock
                || block instanceof GrindstoneBlock
                || block instanceof ButtonBlock
                || block instanceof LoomBlock
                || block instanceof CraftingTableBlock
                || block instanceof CartographyTableBlock
                || block instanceof EnchantmentTableBlock
                || block instanceof SmithingTableBlock
                || block instanceof StonecutterBlock
        ) {
            return true;
        }
        return getIntractableBlocks().contains(block);
    }

    public static List<String> getIntractableBlocks() {
        //TODO Add config option to add blocks to this list
        return List.of();
    }
}
