package com.redpxnda.respawnobelisks.facet.kept;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class KeptOffhandModule implements KeptItemsModule {
    public ItemStack item = ItemStack.EMPTY;

    @Override
    public Tag toNbt() {
        return item.save(new CompoundTag());
    }

    @Override
    public void fromNbt(Tag element) {
        if (!(element instanceof CompoundTag compound)) return;
        item = ItemStack.of(compound);
    }

    @Override
    public void restore(ServerPlayer oldPlayer, ServerPlayer player) {
        if (item.isEmpty()) return;

        if (player.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) player.setItemSlot(EquipmentSlot.OFFHAND, item);
        else ObeliskUtils.givePlayerSavedItem(player, item, oldPlayer.getRespawnPosition());
        item = ItemStack.EMPTY;
    }

    @Override
    public void gather(ServerPlayer player) {
        if (!item.isEmpty()) return;
        if (ObeliskUtils.shouldSaveItem(RespawnObelisksConfig.INSTANCE.respawnPerks.offhand.keepOffhand, RespawnObelisksConfig.INSTANCE.respawnPerks.offhand.keepOffhandChance, player.getOffhandItem())) {
            item = player.getOffhandItem().copy();
            player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public void scatter(double x, double y, double z, ServerPlayer player) {
        Containers.dropItemStack(player.level(), x, y, z, item);
        item = ItemStack.EMPTY;
    }

    @Override
    public boolean isEmpty() {
        return item.isEmpty();
    }
}
