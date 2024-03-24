package com.redpxnda.respawnobelisks.facet.kept;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;

public class KeptOffhandModule implements KeptItemsModule {
    public ItemStack item = ItemStack.EMPTY;

    @Override
    public NbtElement toNbt() {
        return item.writeNbt(new NbtCompound());
    }

    @Override
    public void fromNbt(NbtElement element) {
        if (!(element instanceof NbtCompound compound)) return;
        item = ItemStack.fromNbt(compound);
    }

    @Override
    public void restore(ServerPlayerEntity player) {
        if (item.isEmpty()) return;

        if (player.getEquippedStack(EquipmentSlot.OFFHAND).isEmpty()) player.equipStack(EquipmentSlot.OFFHAND, item);
        else player.getInventory().offerOrDrop(item);
        item = ItemStack.EMPTY;
    }

    @Override
    public void gather(ServerPlayerEntity player) {
        if (!item.isEmpty()) return;
        if (ObeliskUtils.shouldSaveItem(RespawnObelisksConfig.INSTANCE.respawnPerks.offhand.keepOffhand, RespawnObelisksConfig.INSTANCE.respawnPerks.offhand.keepOffhandChance, player.getOffHandStack())) {
            item = player.getOffHandStack().copy();
            player.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public void scatter(double x, double y, double z, ServerPlayerEntity player) {
        ItemScatterer.spawn(player.getWorld(), x, y, z, item);
        item = ItemStack.EMPTY;
    }

    @Override
    public boolean isEmpty() {
        return item.isEmpty();
    }
}
