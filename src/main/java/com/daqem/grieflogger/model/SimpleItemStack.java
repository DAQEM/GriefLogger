package com.daqem.grieflogger.model;

import com.daqem.grieflogger.GriefLogger;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class SimpleItemStack {

    private final Item item;
    private int count;
    private final DataComponentPatch tag;

    public SimpleItemStack(ItemStack itemStack) {
        this(itemStack.getItem(), itemStack.getCount(), itemStack.getComponentsPatch());
    }

    public SimpleItemStack(Identifier itemLocation, int count, DataComponentPatch tag) {
        this.item = BuiltInRegistries.ITEM.getValue(itemLocation);
        this.count = count;
        this.tag = tag;
    }

    public SimpleItemStack(Item item, int count, DataComponentPatch tag) {
        this.item = item;
        this.count = count;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        //DOES NOT CHECK COUNT
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleItemStack that = (SimpleItemStack) o;
        return Objects.equals(item, that.item) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, count, tag);
    }

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    public DataComponentPatch getTag() {
        return tag;
    }

    public boolean hasTag() {
        return tag != null;
    }

    public boolean hasNoTag() {
        return tag == null;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addCount(int count) {
        this.count += count;
    }

    public byte @Nullable [] getTagBytes(Level level) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        try {
            Tag nbtTag = DataComponentPatch.CODEC.encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag)
                    .getOrThrow(IllegalStateException::new);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            NbtIo.writeCompressed((CompoundTag) nbtTag, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            GriefLogger.LOGGER.error("Failed to serialize item components", e);
            return null;
        }
    }

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(item, count);
        itemStack.applyComponents(tag);
        return itemStack;
    }

    public boolean isEmpty() {
        return item.equals(ItemStack.EMPTY.getItem()) || count == 0;
    }
}