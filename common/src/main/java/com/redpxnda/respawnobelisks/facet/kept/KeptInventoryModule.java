package com.redpxnda.respawnobelisks.facet.kept;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KeptInventoryModule implements KeptItemsModule {
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
        items.forEach(i -> {
            if (!i.isEmpty()) ObeliskUtils.givePlayerSavedItem(player, i, oldPlayer.getRespawnPosition());
        });
        items.clear();
    }

    @Override
    public void gather(ServerPlayer player) {
        if (!items.isEmpty()) return;
        int index = 0;
        for (ItemStack stack : player.getInventory().items) {
            boolean isHotbar = index < 9;

            boolean keep = isHotbar ? RespawnObelisksConfig.INSTANCE.respawnPerks.hotbar.keepHotbar : RespawnObelisksConfig.INSTANCE.respawnPerks.inventory.keepInventory;
            double chance = isHotbar ? RespawnObelisksConfig.INSTANCE.respawnPerks.hotbar.keepHotbarChance : RespawnObelisksConfig.INSTANCE.respawnPerks.inventory.keepInventoryChance;

            if (ObeliskUtils.shouldSaveItem(keep, chance, stack)) {
                items.add(stack);
                player.getInventory().items.set(index, ItemStack.EMPTY);
            }

            index++;
        }
    }

    @Override
    public void scatter(double x, double y, double z, ServerPlayer player) {
        items.forEach(item -> Containers.dropItemStack(player.level(), x, y, z, item));
        items.clear();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
