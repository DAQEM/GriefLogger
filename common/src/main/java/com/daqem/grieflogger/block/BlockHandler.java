package com.daqem.grieflogger.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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
                || block instanceof EnchantingTableBlock
                || block instanceof SmithingTableBlock
                || block instanceof StonecutterBlock
        ) {
            return true;
        }
        return getIntractableBlocks().contains(block.arch$registryName().toString());
    }

    public static List<String> getIntractableBlocks() {
        //TODO Add config option to add blocks to this list
        return List.of();
    }

    public static Optional<BlockPos> getSecondDoorPosition(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof DoorBlock) {
            if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                return Optional.of(pos.above());
            }
            else if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                return Optional.of(pos.below());
            }
        }
        return Optional.empty();
    }
}
