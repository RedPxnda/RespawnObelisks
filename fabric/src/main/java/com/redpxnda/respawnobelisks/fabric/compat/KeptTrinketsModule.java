package com.redpxnda.respawnobelisks.fabric.compat;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.kept.KeptItemsModule;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeptTrinketsModule implements KeptItemsModule {
    public Map<String, Map<String, List<ItemStack>>> trinketInventory = new HashMap<>();

    @Override
    public Tag toNbt() {
        CompoundTag root = new CompoundTag();
        trinketInventory.forEach((groupKey, map) -> {
            CompoundTag group = new CompoundTag();
            map.forEach((slotKey, inv) -> {
                ListTag items = new ListTag();
                items.addAll(inv.stream().map(s -> s.save(new CompoundTag())).toList());
                group.put(slotKey, items);
            });
            root.put(groupKey, group);
        });
        return root;
    }

    @Override
    public void fromNbt(Tag element) {
        if (!(element instanceof CompoundTag compound)) return;
        trinketInventory.clear();
        compound.getAllKeys().forEach(groupKey -> {
            CompoundTag rawGroup = compound.getCompound(groupKey);
            Map<String, List<ItemStack>> group = new HashMap<>();
            rawGroup.getAllKeys().forEach(slotKey -> {
                ListTag rawSlot = compound.getList(slotKey, Tag.TAG_COMPOUND);
                List<ItemStack> slot = new ArrayList<>();
                for (Tag nbtElement : rawSlot) {
                    if (nbtElement instanceof CompoundTag comp) slot.add(ItemStack.of(comp));
                }
                group.put(slotKey, slot);
            });
            trinketInventory.put(groupKey, group);
        });
    }

    @Override
    public void restore(ServerPlayer oldPlayer, ServerPlayer player) {
        TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
            component.getInventory().forEach((group, slots) -> {
                Map<String, List<ItemStack>> storedGroup = trinketInventory.get(group);
                if (storedGroup == null) return;

                slots.forEach((slot, inv) -> {
                    List<ItemStack> storedInv = storedGroup.get(slot);
                    if (storedInv == null) return;

                    for (int i = 0; i < storedInv.size(); i++) {
                        if (i >= inv.getContainerSize()) continue;

                        ItemStack stack = storedInv.get(i);
                        if (stack.isEmpty()) continue;

                        ItemStack prev = inv.getItem(i);

                        if (!prev.isEmpty()) ObeliskUtils.givePlayerSavedItem(player, stack, oldPlayer.getRespawnPosition());
                        else inv.setItem(i, stack.copy());
                    }
                });
            });
        });
        trinketInventory.clear();
    }

    @Override
    public void gather(ServerPlayer player) {
        if (!trinketInventory.isEmpty()) return;
        TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
            component.getInventory().forEach((group, slots) -> {
                Map<String, List<ItemStack>> storedGroup = new HashMap<>();
                slots.forEach((slot, inv) -> {
                    List<ItemStack> storedInv = new ArrayList<>();

                    for (int i = 0; i < inv.getContainerSize(); i++) {
                        ItemStack stack = inv.getItem(i);
                        if (!ObeliskUtils.shouldSaveItem(RespawnObelisksConfig.INSTANCE.respawnPerks.armor.keepArmor, RespawnObelisksConfig.INSTANCE.respawnPerks.armor.keepArmorChance, stack))
                            stack = ItemStack.EMPTY;
                        if (!stack.isEmpty()) storedInv.add(stack);
                        if (!stack.isEmpty()) inv.setItem(i, ItemStack.EMPTY);
                    }

                    storedGroup.put(slot, storedInv);
                });
                trinketInventory.put(group, storedGroup);
            });
        });
    }

    @Override
    public void scatter(double x, double y, double z, ServerPlayer player) {
        trinketInventory.forEach((group, slots) -> slots.forEach((key, items) -> items.forEach(item -> Containers.dropItemStack(player.level(), x, y, z, item))));
        trinketInventory.clear();
    }

    @Override
    public boolean isEmpty() {
        for (Map<String, List<ItemStack>> map : trinketInventory.values()) {
            for (List<ItemStack> list : map.values()) {
                if (!list.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
