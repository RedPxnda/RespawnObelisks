package com.redpxnda.respawnobelisks.forge.compat;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.kept.KeptItemsModule;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeptCuriosModule implements KeptItemsModule {
    public Map<String, List<ItemStack>> curiosInventory = new HashMap<>();

    @Override
    public Tag toNbt() {
        CompoundTag root = new CompoundTag();
        curiosInventory.forEach((slotKey, items) -> {
            ListTag list = new ListTag();
            list.addAll(items.stream().map(s -> s.save(new CompoundTag())).toList());
            root.put(slotKey, list);
        });
        return root;
    }

    @Override
    public void fromNbt(Tag element) {
        if (!(element instanceof CompoundTag compound)) return;
        curiosInventory.clear();
        compound.getAllKeys().forEach(slotKey -> {
            ListTag rawItems = compound.getList(slotKey, Tag.TAG_COMPOUND);
            List<ItemStack> items = new ArrayList<>();
            for (Tag nbtElement : rawItems) {
                if (nbtElement instanceof CompoundTag comp) items.add(ItemStack.of(comp));
            }
            curiosInventory.put(slotKey, items);
        });
    }

    @Override
    public void restore(ServerPlayer oldPlayer, ServerPlayer player) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getCurios().forEach((slot, data) -> {
            List<ItemStack> storedSlots = curiosInventory.get(slot);
            if (storedSlots == null) return;
            IDynamicStackHandler inv = data.getStacks();

            for (int i = 0; i < storedSlots.size(); i++) {
                if (i >= inv.getSlots()) continue;

                ItemStack stack = storedSlots.get(i);
                if (stack.isEmpty()) continue;

                //ItemStack prev = inv.getStackInSlot(i);

                /*if (!prev.isEmpty()) */ObeliskUtils.givePlayerSavedItem(player, stack, oldPlayer.getRespawnPosition()); // curios is fucking weird and drops all the changes i make, i just give it to player instead
                // else inv.setStackInSlot(i, stack.copy());
            }
        }));
        curiosInventory.clear();
    }

    @Override
    public void gather(ServerPlayer player) {
        if (!isEmpty()) return;
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getCurios().forEach((slot, data) -> {
            List<ItemStack> storedItems = new ArrayList<>();
            IDynamicStackHandler inv = data.getStacks();

            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!ObeliskUtils.shouldSaveItem(RespawnObelisksConfig.INSTANCE.respawnPerks.trinkets.keepTrinkets, RespawnObelisksConfig.INSTANCE.respawnPerks.trinkets.keepTrinketsChance, stack))
                    stack = ItemStack.EMPTY;
                if (!stack.isEmpty()) storedItems.add(stack);
                if (!stack.isEmpty()) inv.setStackInSlot(i, ItemStack.EMPTY);
            }

            curiosInventory.put(slot, storedItems);
        }));
    }

    @Override
    public void scatter(double x, double y, double z, ServerPlayer player) {
        curiosInventory.forEach((key, items) -> items.forEach(item -> Containers.dropItemStack(player.level(), x, y, z, item)));
        curiosInventory.clear();
    }

    @Override
    public boolean isEmpty() {
        return curiosInventory.isEmpty();
    }
}
