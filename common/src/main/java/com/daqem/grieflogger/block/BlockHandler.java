package com.daqem.grieflogger.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;
import java.util.Optional;

public class BlockHandler {
    private static final TagKey<Block> C_CHESTS = TagKey.create(Registries.BLOCK, new ResourceLocation("c", "chests"));
    private static final TagKey<Block> C_WORKBENCHES = TagKey.create(Registries.BLOCK, new ResourceLocation("c", "workbenches"));

    public static boolean isBlockInteractable(Block block) {
        BlockState state = block.defaultBlockState();

        if (state.is(BlockTags.DOORS) ||
                state.is(BlockTags.FENCE_GATES) ||
                state.is(BlockTags.TRAPDOORS) ||
                state.is(BlockTags.BUTTONS) ||
                state.is(BlockTags.SIGNS) ||
                state.is(BlockTags.BEDS)) {
            return true;
        }

        if (state.is(C_CHESTS) || state.is(C_WORKBENCHES)) {
            return true;
        }

        if (block instanceof EntityBlock) {
            return !(block instanceof BannerBlock);
        }

        if (block instanceof LeverBlock
                || block instanceof NoteBlock
                || block instanceof DiodeBlock
                || block instanceof GrindstoneBlock
                || block instanceof LoomBlock
                || block instanceof StonecutterBlock
                || block instanceof LecternBlock) {
            return true;
        }

        return getInteractableBlocks().contains(block.arch$registryName().toString());
    }

    public static List<String> getInteractableBlocks() {
        //TODO Add config option to add blocks to this list
        return List.of();
    }

    public static Optional<BlockPos> getSecondDoorPosition(BlockPos pos, BlockState state) {
        if (state.hasProperty(DoorBlock.HALF)) {
            DoubleBlockHalf half = state.getValue(DoorBlock.HALF);
            return Optional.of(half == DoubleBlockHalf.LOWER ? pos.above() : pos.below());
        }
        return Optional.empty();
    }
}
