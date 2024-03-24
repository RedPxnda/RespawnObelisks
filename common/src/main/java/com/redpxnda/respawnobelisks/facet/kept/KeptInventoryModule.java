package com.redpxnda.respawnobelisks.facet.kept;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;

import java.util.ArrayList;
import java.util.List;

public class KeptInventoryModule implements KeptItemsModule {
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
        items.forEach(i -> {
            if (!i.isEmpty()) player.getInventory().offerOrDrop(i);
        });
        items.clear();
    }

    @Override
    public void gather(ServerPlayerEntity player) {
        if (!items.isEmpty()) return;
        int index = 0;
        for (ItemStack stack : player.getInventory().main) {
            boolean isHotbar = index < 9;

            boolean keep = isHotbar ? RespawnObelisksConfig.INSTANCE.respawnPerks.hotbar.keepHotbar : RespawnObelisksConfig.INSTANCE.respawnPerks.inventory.keepInventory;
            double chance = isHotbar ? RespawnObelisksConfig.INSTANCE.respawnPerks.hotbar.keepHotbarChance : RespawnObelisksConfig.INSTANCE.respawnPerks.inventory.keepInventoryChance;

            if (ObeliskUtils.shouldSaveItem(keep, chance, stack)) {
                items.add(stack);
                player.getInventory().main.set(index, ItemStack.EMPTY);
            }
            else items.add(ItemStack.EMPTY);

            index++;
        }
    }

    @Override
    public void scatter(double x, double y, double z, ServerPlayerEntity player) {
        items.forEach(item -> ItemScatterer.spawn(player.getWorld(), x, y, z, item));
        items.clear();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
