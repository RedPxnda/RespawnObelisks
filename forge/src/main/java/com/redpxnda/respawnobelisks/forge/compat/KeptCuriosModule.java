package com.redpxnda.respawnobelisks.forge.compat;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.kept.KeptItemsModule;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeptCuriosModule implements KeptItemsModule {
    public Map<String, List<ItemStack>> curiosInventory = new HashMap<>();

    @Override
    public NbtElement toNbt() {
        NbtCompound root = new NbtCompound();
        curiosInventory.forEach((slotKey, items) -> {
            NbtList list = new NbtList();
            list.addAll(items.stream().map(s -> s.writeNbt(new NbtCompound())).toList());
            root.put(slotKey, list);
        });
        return root;
    }

    @Override
    public void fromNbt(NbtElement element) {
        if (!(element instanceof NbtCompound compound)) return;
        curiosInventory.clear();
        compound.getKeys().forEach(slotKey -> {
            NbtList rawItems = compound.getList(slotKey, NbtElement.COMPOUND_TYPE);
            List<ItemStack> items = new ArrayList<>();
            for (NbtElement nbtElement : rawItems) {
                if (nbtElement instanceof NbtCompound comp) items.add(ItemStack.fromNbt(comp));
            }
            curiosInventory.put(slotKey, items);
        });
    }

    @Override
    public void restore(ServerPlayerEntity player) {
        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            handler.getCurios().forEach((slot, data) -> {
                List<ItemStack> storedSlots = curiosInventory.get(slot);
                if (storedSlots == null) return;
                IDynamicStackHandler inv = data.getStacks();

                for (int i = 0; i < storedSlots.size(); i++) {
                    if (i >= inv.getSlots()) continue;

                    ItemStack stack = storedSlots.get(i);
                    if (stack.isEmpty()) continue;

                    ItemStack prev = inv.getStackInSlot(i);

                    if (!prev.isEmpty()) player.getInventory().offerOrDrop(stack);
                    else inv.setStackInSlot(i, stack.copy());
                }
            });
        });
        curiosInventory.clear();
    }

    @Override
    public void gather(ServerPlayerEntity player) {
        if (!curiosInventory.isEmpty()) return;
        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            handler.getCurios().forEach((slot, data) -> {
                List<ItemStack> storedItems = new ArrayList<>();
                IDynamicStackHandler inv = data.getStacks();

                for (int i = 0; i < inv.getSlots(); i++) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (!ObeliskUtils.shouldSaveItem(RespawnObelisksConfig.INSTANCE.respawnPerks.armor.keepArmor, RespawnObelisksConfig.INSTANCE.respawnPerks.armor.keepArmorChance, stack))
                        stack = ItemStack.EMPTY;
                    storedItems.add(stack);
                    if (!stack.isEmpty()) inv.setStackInSlot(i, ItemStack.EMPTY);
                }

                curiosInventory.put(slot, storedItems);
            });
        });
    }

    @Override
    public void scatter(double x, double y, double z, ServerPlayerEntity player) {
        curiosInventory.forEach((key, items) -> items.forEach(item -> ItemScatterer.spawn(player.getWorld(), x, y, z, item)));
        curiosInventory.clear();
    }
}
