package com.redpxnda.respawnobelisks.registry.block.entity;

import com.redpxnda.respawnobelisks.config.ChargeConfig;
import com.redpxnda.respawnobelisks.config.ObeliskCoreConfig;
import com.redpxnda.respawnobelisks.config.TrustedPlayersConfig;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.ObeliskInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class RespawnObeliskBlockEntity extends BlockEntity {
    protected final List<Consumer<CompoundTag>> loadConsumers = new ArrayList<>();

    private ItemStack coreItem;
    private CompoundTag playerCharges;
    private boolean hasLimboEntity;
    private long lastRespawn;
    private long lastCharge;
    private String obeliskName = "";
    private Component obeliskNameComponent = null;
    public final Map<UUID, ObeliskInventory> storedItems = new HashMap<>();
    public boolean hasStoredItems = false;
    public boolean hasTeleportingEntity = false;

    public RespawnObeliskBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModRegistries.RESPAWN_OBELISK_BE.get(), pPos, pBlockState);
        coreItem = ItemStack.EMPTY;
        playerCharges = new CompoundTag();
        lastRespawn = 0;
        lastCharge = 0;
        hasLimboEntity = false;
    }

    public boolean isPlayerTrusted(String username) {
        if (!CoreUtils.hasCapability(coreItem, CoreUtils.Capability.PROTECT)) return true;
        if (TrustedPlayersConfig.enablePlayerTrust && this.coreItem.getOrCreateTag().contains("RespawnObeliskData")) {
            CompoundTag obeliskData = this.coreItem.getTag().getCompound("RespawnObeliskData");
            if (obeliskData.contains("TrustedPlayers")) {
                ListTag list = obeliskData.getList("TrustedPlayers", 8);
                return list.contains(StringTag.valueOf(username));
            }
        }
        return true;
    }
    public Component getObeliskNameComponent() {
        return obeliskNameComponent;
    }
    public String getObeliskName() {
        return obeliskName;
    }
    public boolean hasLimboEntity() {
        return hasLimboEntity;
    }
    public long getLastRespawn() {
        return lastRespawn;
    }
    public void setLastRespawn(long lastRespawn) {
        this.lastRespawn = lastRespawn;
    }
    public long getLastCharge() {
        return lastCharge;
    }
    public void setLastCharge(long lastCharge) {
        this.lastCharge = lastCharge;
    }
    public double getCharge(@Nullable Player player) {
        if (ChargeConfig.perPlayerCharge && player != null) {
            if (!playerCharges.contains(player.getScoreboardName(), 6))
                playerCharges.putDouble(player.getScoreboardName(), 0);
            return playerCharges.getDouble(player.getScoreboardName());
        }
        if (coreItem.isEmpty()) return 0;
        return coreItem.getOrCreateTag().getCompound("RespawnObeliskData").getDouble("Charge");
    }
    public double getMaxCharge() {
        if (coreItem.isEmpty()) return 0;
        return Math.min(ObeliskCoreConfig.maxMaxCharge, coreItem.getOrCreateTag().getCompound("RespawnObeliskData").getDouble("MaxCharge"));
    }
    public CompoundTag getItemNbt() {
        return coreItem.save(new CompoundTag());
    }
    public void setItemNbt(CompoundTag tag) {
        coreItem = ItemStack.of(tag);
    }
    public void setCharge(Player player, double charge) {
        if (ChargeConfig.perPlayerCharge) {
            playerCharges.putDouble(player.getScoreboardName(), charge);
            return;
        }
        coreItem.getOrCreateTag().getCompound("RespawnObeliskData").putDouble("Charge", charge);
    }
    public void setItem(ItemStack stack) {
        coreItem = stack;
    }
    public ItemStack getItemStack() {
        return coreItem;
    }

    public BlockEntity decreaseCharge(Player player, double amnt) {
        this.setCharge(player, Mth.clamp(this.getCharge(player) - amnt, 0, this.getMaxCharge()));
        this.syncWithClient();
        return this;
    }

    public BlockEntity increaseCharge(Player player, double amnt) {
        this.setCharge(player, Mth.clamp(this.getCharge(player) + amnt, 0, this.getMaxCharge()));
        this.syncWithClient();
        return this;
    }

    public void restoreSavedItems(Player player) {
        ObeliskInventory inv = storedItems.get(player.getUUID());
        if (inv == null) return;
        if (!inv.isItemsEmpty()) {
            inv.items.forEach(i -> {
                if (!i.isEmpty()) player.getInventory().placeItemBackInInventory(i);
            });
            inv.items.clear();
        }
        if (!inv.isArmorEmpty()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType().equals(EquipmentSlot.Type.ARMOR)) {
                    if (inv.armor.size() <= slot.getIndex())
                        continue;
                    if (player.getItemBySlot(slot).isEmpty())
                        player.setItemSlot(slot, inv.armor.get(slot.getIndex()));
                    else
                        player.getInventory().placeItemBackInInventory(inv.armor.get(slot.getIndex()));
                }
            }
            inv.armor.clear();
        }
        if (!inv.isOffhandEmpty()) {
            if (player.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty())
                player.setItemSlot(EquipmentSlot.OFFHAND, inv.offhand.get(0));
            else
                player.getInventory().placeItemBackInInventory(inv.offhand.get(0));
            inv.offhand.clear();
        }
        if (inv.isXpEmpty()) {
            player.giveExperiencePoints(inv.xp);
            inv.xp = 0;
        }
        syncWithClient();
    }

    public void syncWithClient() {
        if (level.isClientSide) return;
        updateHasSavedItems();
        setChanged(this.level, this.getBlockPos(), this.getBlockState());
    }

    public void updateHasSavedItems() {
        boolean isEmpty = true;
        for (ObeliskInventory inv : storedItems.values()) {
            if (!inv.isEmpty()) {
                isEmpty = false;
                break;
            }
        }
        this.hasStoredItems = !isEmpty;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.coreItem = ItemStack.of(tag.getCompound("Item"));
        this.playerCharges = tag.getCompound("PlayerCharges");
        this.lastRespawn = tag.getLong("LastRespawn");
        this.lastCharge = tag.getLong("LastCharge");
        this.hasLimboEntity = tag.getBoolean("HasLimboEntity");
        this.hasStoredItems = tag.getBoolean("HasStoredItems");
        this.obeliskName = tag.getString("Name");
        if (tag.contains("StoredItems", 10)) {
            CompoundTag stored = tag.getCompound("StoredItems");
            for (String key : stored.getAllKeys()) {
                this.storedItems.put(UUID.fromString(key), ObeliskInventory.readFromNbt(stored.getCompound(key)));
            }
        }
        this.obeliskNameComponent = Component.Serializer.fromJson(tag.getString("Name"));
        this.hasTeleportingEntity = tag.getBoolean("HasTeleportingEntity");
        loadConsumers.forEach(c -> c.accept(tag));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.saveData(tag, true);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveData(tag, false);
        return tag;
    }

    private void saveData(CompoundTag tag, boolean saveItems) {
        tag.put("Item", coreItem.save(new CompoundTag()));
        tag.put("PlayerCharges", playerCharges);
        tag.putLong("LastRespawn", lastRespawn);
        tag.putLong("LastCharge", lastCharge);
        tag.putBoolean("HasLimboEntity", hasLimboEntity);
        tag.putBoolean("HasStoredItems", hasStoredItems);
        tag.putString("Name", obeliskName);
        if (saveItems) {
            CompoundTag allPlayers = new CompoundTag();
            for (UUID uuid : storedItems.keySet()) {
                allPlayers.put(uuid.toString(), storedItems.get(uuid).saveToNbt());
            }
            tag.put("StoredItems", allPlayers);
        }
        tag.putBoolean("HasTeleportingEntity", hasTeleportingEntity);
    }

    protected static void setChanged(Level pLevel, BlockPos pPos, BlockState pState) {
        BlockEntity.setChanged(pLevel, pPos, pState);
        pLevel.sendBlockUpdated(pPos, pLevel.getBlockState(pPos), pState, 3);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState state, RespawnObeliskBlockEntity blockEntity) {
        if (level.getGameTime() % 100 == 0) {
            if (!level.isClientSide) {
                blockEntity.checkLimbo(true);
            }
        }
    }

    public void updateObeliskName() {
        if (this.coreItem.getOrCreateTag().contains("display")) {
            CompoundTag displayData = this.coreItem.getTag().getCompound("display");
            if (displayData.contains("Name")) {
                obeliskName = displayData.getString("Name");
                return;
            }
        }
        obeliskName = "";
    }

    public void checkLimbo(boolean shouldSync) {
        if (this.coreItem.getOrCreateTag().contains("RespawnObeliskData")) {
            CompoundTag obeliskData = this.coreItem.getTag().getCompound("RespawnObeliskData");
            if (obeliskData.contains("SavedEntities")) {
                ListTag list = obeliskData.getList("SavedEntities", 10);
                for (Tag tag : list) {
                    if (
                            level instanceof ServerLevel serverLevel &&
                                    tag instanceof CompoundTag compound &&
                                    compound.contains("uuid") &&
                                    compound.contains("type") &&
                                    compound.contains("data")
                    ) {
                        Entity entity = serverLevel.getEntity(compound.getUUID("uuid"));
                        if (entity == null || !entity.isAlive()) {
                            this.hasLimboEntity = true;
                            if (shouldSync) this.syncWithClient();
                            return;
                        }
                    }
                }
            }
        }
        this.hasLimboEntity = false;
        if (shouldSync) this.syncWithClient();
    }
}
