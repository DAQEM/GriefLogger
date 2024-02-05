package com.daqem.grieflogger.model;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class SimpleItemStack {

    private final Item item;
    private int count;
    private final CompoundTag tag;

    public SimpleItemStack(ItemStack itemStack) {
        this(itemStack.getItem(), itemStack.getCount(), itemStack.getTag());
    }

    public SimpleItemStack(ResourceLocation itemLocation, int count, byte[] tag) {
        this.item = Registry.ITEM.get(itemLocation);
        this.count = count;
        CompoundTag compoundTag = null;
        if (tag != null) {
            try {
                compoundTag = TagParser.parseTag(new String(tag));
            } catch (CommandSyntaxException ignored) {
            }
        }
        this.tag = compoundTag;
    }

    public SimpleItemStack(Item item, int count, CompoundTag tag) {
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

    public CompoundTag getTag() {
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

    public byte @Nullable [] getTagBytes() {
        if (tag == null) {
            return null;
        }
        return tag.toString().getBytes(StandardCharsets.US_ASCII);
    }

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(item, count);
        itemStack.setTag(tag);
        return itemStack;
    }

    public boolean isEmpty() {
        return item.equals(ItemStack.EMPTY.getItem()) || count == 0;
    }
}
