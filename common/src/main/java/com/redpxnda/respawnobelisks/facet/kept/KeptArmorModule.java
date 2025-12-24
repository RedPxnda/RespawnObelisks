package com.redpxnda.respawnobelisks.facet.kept;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KeptArmorModule implements KeptItemsModule {
    public List<ItemStack> items = new ArrayList<>();

    @Override
    public Tag toNbt() {
        ListTag tag = new ListTag();
        items.forEach(stack -> tag.add(stack.save(new CompoundTag())));
        return tag;
    }

    @Override
    public void fromNbt(Tag element) {
        if (!(element instanceof ListTag list)) return;
        items.clear();
        for (Tag itemTag : list) {
            if (itemTag instanceof CompoundTag compound)
                items.add(ItemStack.of(compound));
        }
    }

    @Override
    public void restore(ServerPlayer oldPlayer, ServerPlayer player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType().equals(EquipmentSlot.Type.ARMOR)) {
                if (items.size() <= slot.getIndex()) continue;

                if (player.getItemBySlot(slot).isEmpty()) player.setItemSlot(slot, items.get(slot.getIndex()));
                else ObeliskUtils.givePlayerSavedItem(player, items.get(slot.getIndex()), oldPlayer.getRespawnPosition());
            }
        }
        items.clear();
    }

    @Override
    public void gather(ServerPlayer player) {
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
    public void scatter(double x, double y, double z, ServerPlayer player) {
        items.forEach(item -> Containers.dropItemStack(player.level(), x, y, z, item));
        items.clear();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack i : items) {
            if (!i.isEmpty())
                return false;
        }
        return true;
    }
}
