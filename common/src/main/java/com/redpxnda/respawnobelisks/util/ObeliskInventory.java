package com.redpxnda.respawnobelisks.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.World;

public class ObeliskInventory {
    public final List<ItemStack> items = new ArrayList<>();
    public final List<ItemStack> armor = new ArrayList<>();
    public final List<ItemStack> offhand = new ArrayList<>();
    public int xp = 0;

    public boolean isEmpty() {
        return isItemsEmpty() && isArmorEmpty() && isOffhandEmpty();
    }

    public void dropAll(World level, double x, double y, double z) {
        for (ItemStack stack : items) {
            ItemScatterer.spawn(level, x, y, z, stack);
        }
        for (ItemStack stack : armor) {
            ItemScatterer.spawn(level, x, y, z, stack);
        }
        for (ItemStack stack : offhand) {
            ItemScatterer.spawn(level, x, y, z, stack);
        }
        if (xp > 0) {
            level.spawnEntity(new ExperienceOrbEntity(level, x, y, z, xp));
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

    public NbtCompound saveToNbt() {
        NbtList armor = new NbtList();
        NbtList items = new NbtList();
        NbtCompound offhand = new NbtCompound();
        this.armor.forEach(stack -> armor.add(stack.writeNbt(new NbtCompound())));
        if (!this.offhand.isEmpty()) this.offhand.get(0).writeNbt(offhand);
        this.items.forEach(stack -> items.add(stack.writeNbt(new NbtCompound())));
        NbtCompound allItems = new NbtCompound();
        allItems.put("Armor", armor);
        allItems.put("Items", items);
        allItems.put("Offhand", offhand);
        allItems.putInt("Xp", xp);
        return allItems;
    }

    public static ObeliskInventory readFromNbt(NbtCompound tag) {
        ObeliskInventory inventory = new ObeliskInventory();
        if (tag.contains("Armor", 9)) {
            for (NbtElement itemTag : tag.getList("Armor", 10)) {
                if (itemTag instanceof NbtCompound compound) {
                    inventory.armor.add(ItemStack.fromNbt(compound));
                }
            }
        }
        if (tag.contains("Items", 9)) {
            for (NbtElement itemTag : tag.getList("Items", 10)) {
                if (itemTag instanceof NbtCompound compound) {
                    inventory.items.add(ItemStack.fromNbt(compound));
                }
            }
        }
        if (tag.contains("Offhand", 10)) {
            inventory.offhand.add(ItemStack.fromNbt(tag.getCompound("Offhand")));
        }
        if (tag.contains("Xp", 3)) inventory.xp = tag.getInt("Xp");
        return inventory;
    }
}
