package com.redpxnda.respawnobelisks.registry.block.entity;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RadiantFlameBlockEntity extends BlockEntity {
    public static double getDefaultCharge() {
        return 60;
    }
    public static int getDefaultTime() {
        return RespawnObelisksConfig.INSTANCE.radiantFlame.lifetime;
    }

    public double charge;
    public double initialCharge;
    public int timeRemaining;
    public @Nullable UUID owner;
    public final Multimap<GlobalPos, ServerPlayer> respawningPlayers = Multimaps.newMultimap(new ConcurrentHashMap<>(), HashSet::new);

    public RadiantFlameBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.radiantFlameBlockEntity.get(), pos, state);
        charge = getDefaultCharge();
        timeRemaining = getDefaultTime();
    }

    @Override
    public void load(CompoundTag tag) {
        charge = tag.getDouble("Charge");
        initialCharge = tag.getDouble("InitialCharge");
        timeRemaining = tag.getInt("TimeRemaining");
        if (tag.contains("Owner")) owner = tag.getUUID("Owner");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveData(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveData(tag);
        return tag;
    }

    private void saveData(CompoundTag tag) {
        tag.putDouble("Charge", charge);
        tag.putDouble("InitialCharge", initialCharge);
        tag.putInt("TimeRemaining", timeRemaining);
        if (owner != null) tag.putUUID("Owner", owner);
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public double getInitialCharge() {
        return initialCharge;
    }

    public void setInitialCharge(double initialCharge) {
        this.initialCharge = initialCharge;
    }

    public double getCharge() {
        return charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public void decreaseCharge(double amnt) {
        setCharge(Math.max(getCharge() - amnt, 0));
        removeIfNoCharge();
        syncWithClient();
    }

    public void increaseCharge(double amnt) {
        setCharge(Math.max(getCharge() + amnt, 0));
        removeIfNoCharge();
        syncWithClient();
    }

    public void syncWithClient() {
        if (level == null || level.isClientSide) return;
        setChanged(this.level, this.getBlockPos(), this.getBlockState());
    }

    public void removeIfNoCharge() {
        if (charge <= 0)
            remove();
    }

    public void remove() {
        if (getLevel() != null)
            getLevel().removeBlock(getBlockPos(), false);
    }

    public void remove(Level level, BlockPos pos) {
        level.removeBlock(pos, false);
    }

    public void tick(Level level, BlockPos blockPos, BlockState state) {
        reduceTime(level, blockPos, state);

        if (level.getGameTime() % 20 == 0 && RespawnObelisksConfig.INSTANCE.radiantFlame.radianceReduction != 0)
            decreaseCharge(RespawnObelisksConfig.INSTANCE.radiantFlame.radianceReduction);
    }

    public void reduceTime(Level level, BlockPos blockPos, BlockState state) {
        if (timeRemaining > 0)
            timeRemaining -= 1;
        else if (timeRemaining == 0) {
            remove(level, blockPos);
        }
    }
}
