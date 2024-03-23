package com.redpxnda.respawnobelisks.facet.kept;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;

import java.util.ArrayList;
import java.util.List;

public class KeptArmorModule implements KeptItemsModule {
    public List<ItemStack> items = new ArrayList<>();

    @Override
    public NbtElement toNbt() {
        NbtList tag = new NbtList();
        items.forEach(stack -> tag.add(stack.writeNbt(new NbtCompound())));
        return tag;
    }

    @Override
    public void fromNbt(NbtElement element) {
        if (!(element instanceof NbtList list)) return;
        items.clear();
        for (NbtElement itemTag : list) {
            if (itemTag instanceof NbtCompound compound)
                items.add(ItemStack.fromNbt(compound));
        }
    }

    @Override
    public void restore(ServerPlayerEntity player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType().equals(EquipmentSlot.Type.ARMOR)) {
                if (items.size() <= slot.getEntitySlotId()) continue;

                if (player.getEquippedStack(slot).isEmpty()) player.equipStack(slot, items.get(slot.getEntitySlotId()));
                else player.getInventory().offerOrDrop(items.get(slot.getEntitySlotId()));
            }
        }
        items.clear();
    }

    @Override
    public void gather(ServerPlayerEntity player) {
        if (!items.isEmpty()) return;
        items = new ArrayList<>(
                player.getInventory().armor.stream().map(i -> {
                    if (ObeliskUtils.shouldSaveItem(RespawnObelisksConfig.INSTANCE.respawnPerks.armor.keepArmor, RespawnObelisksConfig.INSTANCE.respawnPerks.armor.keepArmorChance, i)) {
                        int index = player.getInventory().armor.indexOf(i);
                        player.getInventory().armor.set(index, ItemStack.EMPTY);
                        return i;
                    } else {
                        return ItemStack.EMPTY;
                    }
                }).toList()
        );
    }

    @Override
    public void scatter(double x, double y, double z, ServerPlayerEntity player) {
        items.forEach(item -> ItemScatterer.spawn(player.getWorld(), x, y, z, item));
        items.clear();
    }
}
