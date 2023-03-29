package com.redpxnda.respawnobelisks.registry.block.entity;

import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RespawnObeliskBlockEntity extends BlockEntity {
    private static CompoundTag setupNbt(RespawnObeliskBlockEntity be, Item core) {
        if (!be.itemNbt.contains("id")) be.itemNbt.putString("id", Registry.ITEM.getKey(core).toString());
        if (!be.itemNbt.contains("Count")) be.itemNbt.putInt("Count", 1);
        if (!be.itemNbt.contains("tag")) be.itemNbt.put("tag", new CompoundTag());
        CompoundTag tag = be.itemNbt.getCompound("tag");
        if (!tag.contains("RespawnObeliskData")) be.itemNbt.getCompound("tag").put("RespawnObeliskData", new CompoundTag());
        if (!tag.getCompound("RespawnObeliskData").contains("Charge")) be.itemNbt.getCompound("tag").getCompound("RespawnObeliskData").putDouble("Charge", 0);
        if (!tag.getCompound("RespawnObeliskData").contains("MaxCharge")) be.itemNbt.getCompound("tag").getCompound("RespawnObeliskData").putDouble("MaxCharge", 100);
        return be.itemNbt;
    }

    private CompoundTag itemNbt;
    private boolean hasLimboEntity;
    private long lastRespawn;
    private long lastCharge;
    private String obeliskName = "";
    private Component obeliskNameComponent = null;

    public RespawnObeliskBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModRegistries.RESPAWN_OBELISK_BE.get(), pPos, pBlockState);
        itemNbt = new CompoundTag();
        lastRespawn = 0;
        lastCharge = 0;
        hasLimboEntity = false;
        if (pBlockState.getBlock() instanceof RespawnObeliskBlock block)
            setupNbt(this, block.CORE_ITEM);
    }

    public boolean isPlayerTrusted(String username) {
        if (this.itemNbt.contains("tag") && this.itemNbt.getCompound("tag").contains("RespawnObeliskData")) {
            CompoundTag obeliskData = this.itemNbt.getCompound("tag").getCompound("RespawnObeliskData");
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
    public void setHasLimboEntity(boolean hasLimboEntity) {
        this.hasLimboEntity = hasLimboEntity;
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
    public double getCharge() {
        if (itemNbt.isEmpty()) return 0;
        return itemNbt.getCompound("tag").getCompound("RespawnObeliskData").getDouble("Charge");
    }
    public double getMaxCharge() {
        if (itemNbt.isEmpty()) return 0;
        return itemNbt.getCompound("tag").getCompound("RespawnObeliskData").getDouble("MaxCharge");
    }
    public static double getCharge(CompoundTag tag) {
        return tag.getCompound("RespawnObeliskData").getDouble("Charge");
    }
    public static double getMaxCharge(CompoundTag tag) {
        return tag.getCompound("RespawnObeliskData").getDouble("MaxCharge");
    }
    public CompoundTag getItemNbt() {
        return itemNbt;
    }
    public void setItemNbt(CompoundTag tag) {
        itemNbt = tag;
    }
    public void setCharge(double charge) {
        itemNbt.getCompound("tag").getCompound("RespawnObeliskData").putDouble("Charge", charge);
    }
    public void setItem(ItemStack stack) {
        this.itemNbt.put("tag", stack.getOrCreateTag());
        this.itemNbt.putString("id", Registry.ITEM.getKey(stack.getItem()).toString());
        this.itemNbt.putInt("Count", stack.getCount());
    }
    public ItemStack getItemStack() {
        return ItemStack.of(itemNbt);
    }

    public BlockEntity decreaseCharge(double amnt, Level level, BlockPos pos, BlockState state) {
        this.setCharge(Mth.clamp(this.getCharge() - amnt, 0, this.getMaxCharge()));
        setChanged(level, pos, state);
        return this;
    }

    public BlockEntity increaseCharge(double amnt, Level level, BlockPos pos, BlockState state) {
        this.setCharge(Mth.clamp(this.getCharge() + amnt, 0, this.getMaxCharge()));
        setChanged(level, pos, state);
        return this;
    }

    public void syncWithClient(Level level, BlockPos pos, BlockState state) {
        setChanged(level, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.itemNbt = tag.getCompound("Item");
        this.lastRespawn = tag.getLong("LastRespawn");
        this.lastCharge = tag.getLong("LastCharge");
        this.hasLimboEntity = tag.getBoolean("HasLimboEntity");
        this.obeliskName = tag.getString("Name");
        this.obeliskNameComponent = Component.Serializer.fromJson(tag.getString("Name"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Item", this.itemNbt);
        tag.putLong("LastRespawn", this.lastRespawn);
        tag.putLong("LastCharge", this.lastCharge);
        tag.putBoolean("HasLimboEntity", this.hasLimboEntity);
        tag.putString("Name", this.obeliskName);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("Item", this.itemNbt);
        tag.putLong("LastRespawn", this.lastRespawn);
        tag.putLong("LastCharge", this.lastCharge);
        tag.putBoolean("HasLimboEntity", this.hasLimboEntity);
        tag.putString("Name", this.obeliskName);
        return tag;
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
            if (!level.isClientSide)
                blockEntity.checkLimbo(level, blockPos, state, true);
        }
    }

    public void updateObeliskName() {
        if (this.itemNbt.contains("tag") && this.itemNbt.getCompound("tag").contains("display")) {
            CompoundTag displayData = this.itemNbt.getCompound("tag").getCompound("display");
            if (displayData.contains("Name")) {
                obeliskName = displayData.getString("Name");
                return;
            }
        }
        obeliskName = "";
    }

    public void checkLimbo(Level level, BlockPos blockPos, BlockState state, boolean shouldSync) {
        if (this.itemNbt.contains("tag") && this.itemNbt.getCompound("tag").contains("RespawnObeliskData")) {
            CompoundTag obeliskData = this.itemNbt.getCompound("tag").getCompound("RespawnObeliskData");
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
                            if (shouldSync) this.syncWithClient(level, blockPos, state);
                            return;
                        }
                    }
                }
            }
        }
        this.hasLimboEntity = false;
        if (shouldSync) this.syncWithClient(level, blockPos, state);
    }
}
