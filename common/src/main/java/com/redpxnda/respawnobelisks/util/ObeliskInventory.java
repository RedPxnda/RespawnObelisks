package com.redpxnda.respawnobelisks.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ObeliskInventory {
    public final List<ItemStack> items = new ArrayList<>();
    public final List<ItemStack> armor = new ArrayList<>();
    public final List<ItemStack> offhand = new ArrayList<>();
    public int xp = 0;

    public boolean isEmpty() {
        return isItemsEmpty() && isArmorEmpty() && isOffhandEmpty();
    }

    public void dropAll(Level level, double x, double y, double z) {
        for (ItemStack stack : items) {
            Containers.dropItemStack(level, x, y, z, stack);
        }
        for (ItemStack stack : armor) {
            Containers.dropItemStack(level, x, y, z, stack);
        }
        for (ItemStack stack : offhand) {
            Containers.dropItemStack(level, x, y, z, stack);
        }
        if (xp > 0) {
            level.addFreshEntity(new ExperienceOrb(level, x, y, z, xp));
        }
    }

    public boolean isItemsEmpty() {
        boolean bl = true;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                bl = false;
                break;
            }
        }
        return bl;
    }

    public boolean isArmorEmpty() {
        boolean bl = true;
        for (ItemStack stack : armor) {
            if (!stack.isEmpty()) {
                bl = false;
                break;
            }
        }
        return bl;
    }

    public boolean isOffhandEmpty() {
        boolean bl = true;
        for (ItemStack stack : offhand) {
            if (!stack.isEmpty()) {
                bl = false;
                break;
            }
        }
        return bl;
    }

    public boolean isXpEmpty() {
        return xp > 0;
    }

    public CompoundTag saveToNbt() {
        ListTag armor = new ListTag();
        ListTag items = new ListTag();
        CompoundTag offhand = new CompoundTag();
        this.armor.forEach(stack -> armor.add(stack.save(new CompoundTag())));
        if (!this.offhand.isEmpty()) this.offhand.get(0).save(offhand);
        this.items.forEach(stack -> items.add(stack.save(new CompoundTag())));
        CompoundTag allItems = new CompoundTag();
        allItems.put("Armor", armor);
        allItems.put("Items", items);
        allItems.put("Offhand", offhand);
        allItems.putInt("Xp", xp);
        return allItems;
    }

    public static ObeliskInventory readFromNbt(CompoundTag tag) {
        ObeliskInventory inventory = new ObeliskInventory();
        if (tag.contains("Armor", 9)) {
            for (Tag itemTag : tag.getList("Armor", 10)) {
                if (itemTag instanceof CompoundTag compound) {
                    inventory.armor.add(ItemStack.of(compound));
                }
            }
        }
        if (tag.contains("Items", 9)) {
            for (Tag itemTag : tag.getList("Items", 10)) {
                if (itemTag instanceof CompoundTag compound) {
                    inventory.items.add(ItemStack.of(compound));
                }
            }
        }
        if (tag.contains("Offhand", 10)) {
            inventory.offhand.add(ItemStack.of(tag.getCompound("Offhand")));
        }
        if (tag.contains("Xp", 3)) inventory.xp = tag.getInt("Xp");
        return inventory;
    }
}
