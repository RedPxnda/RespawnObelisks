package com.redpxnda.respawnobelisks.fabric.compat;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.kept.KeptItemsModule;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeptTrinketsModule implements KeptItemsModule {
    public Map<String, Map<String, List<ItemStack>>> trinketInventory = new HashMap<>();

    @Override
    public NbtElement toNbt() {
        NbtCompound root = new NbtCompound();
        trinketInventory.forEach((groupKey, map) -> {
            NbtCompound group = new NbtCompound();
            map.forEach((slotKey, inv) -> {
                NbtList items = new NbtList();
                items.addAll(inv.stream().map(s -> s.writeNbt(new NbtCompound())).toList());
                group.put(slotKey, items);
            });
            root.put(groupKey, group);
        });
        return root;
    }

    @Override
    public void fromNbt(NbtElement element) {
        if (!(element instanceof NbtCompound compound)) return;
        trinketInventory.clear();
        compound.getKeys().forEach(groupKey -> {
            NbtCompound rawGroup = compound.getCompound(groupKey);
            Map<String, List<ItemStack>> group = new HashMap<>();
            rawGroup.getKeys().forEach(slotKey -> {
                NbtList rawSlot = compound.getList(slotKey, NbtElement.COMPOUND_TYPE);
                List<ItemStack> slot = new ArrayList<>();
                for (NbtElement nbtElement : rawSlot) {
                    if (nbtElement instanceof NbtCompound comp) slot.add(ItemStack.fromNbt(comp));
                }
                group.put(slotKey, slot);
            });
            trinketInventory.put(groupKey, group);
        });
    }

    @Override
    public void restore(ServerPlayerEntity player) {
        TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
            component.getInventory().forEach((group, slots) -> {
                Map<String, List<ItemStack>> storedGroup = trinketInventory.get(group);
                if (storedGroup == null) return;

                slots.forEach((slot, inv) -> {
                    List<ItemStack> storedInv = storedGroup.get(slot);
                    if (storedInv == null) return;

                    for (int i = 0; i < storedInv.size(); i++) {
                        if (i >= inv.size()) continue;

                        ItemStack stack = storedInv.get(i);
                        if (stack.isEmpty()) continue;

                        ItemStack prev = inv.getStack(i);

                        if (!prev.isEmpty()) player.getInventory().offerOrDrop(stack);
                        else inv.setStack(i, stack.copy());
                    }
                });
            });
        });
        trinketInventory.clear();
    }

    @Override
    public void gather(ServerPlayerEntity player) {
        if (!trinketInventory.isEmpty()) return;
        TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
            component.getInventory().forEach((group, slots) -> {
                Map<String, List<ItemStack>> storedGroup = new HashMap<>();
                slots.forEach((slot, inv) -> {
                    List<ItemStack> storedInv = new ArrayList<>();

                    for (int i = 0; i < inv.size(); i++) {
                        ItemStack stack = inv.getStack(i);
                        if (!ObeliskUtils.shouldSaveItem(RespawnObelisksConfig.INSTANCE.respawnPerks.armor.keepArmor, RespawnObelisksConfig.INSTANCE.respawnPerks.armor.keepArmorChance, stack))
                            stack = ItemStack.EMPTY;
                        storedInv.add(stack);
                        if (!stack.isEmpty()) inv.setStack(i, ItemStack.EMPTY);
                    }

                    storedGroup.put(slot, storedInv);
                });
                trinketInventory.put(group, storedGroup);
            });
        });
    }

    @Override
    public void scatter(double x, double y, double z, ServerPlayerEntity player) {
        trinketInventory.forEach((group, slots) -> slots.forEach((key, items) -> items.forEach(item -> ItemScatterer.spawn(player.getWorld(), x, y, z, item))));
        trinketInventory.clear();
    }
}
