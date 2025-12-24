package com.redpxnda.respawnobelisks.facet;

import com.redpxnda.nucleus.codec.tag.TaggableBlock;
import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import com.redpxnda.nucleus.network.PlayerSendable;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.network.SetPriorityChangerPacket;
import com.redpxnda.respawnobelisks.util.RespawnAvailability;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SecondarySpawnPoints implements EntityFacet<CompoundTag> {
    public static FacetKey<SecondarySpawnPoints> KEY;

    public final List<SpawnPoint> points = new ArrayList<>();
    public @Nullable SpawnPoint reorderingTarget;
    public boolean canChooseRespawn = false;
    public boolean canChooseWorldSpawn = false;
    public boolean willRespawnAtWorldSpawn = false;

    public static CompoundTag serializeSpawnPoint(SpawnPoint point) {
        CompoundTag compound = new CompoundTag();
        compound.putString("Dimension", point.dimension().location().toString());
        compound.putInt("x", point.pos().getX());
        compound.putInt("y", point.pos().getY());
        compound.putInt("z", point.pos().getZ());
        compound.putFloat("angle", point.angle());
        compound.putBoolean("forced", point.forced());
        return compound;
    }

    public static SpawnPoint deserializeSpawnPoint(CompoundTag compound) {
        return new SpawnPoint(
                ResourceKey.create(Registries.DIMENSION, new ResourceLocation(compound.getString("Dimension"))),
                new BlockPos(
                        compound.getInt("x"),
                        compound.getInt("y"),
                        compound.getInt("z")
                ),
                compound.getFloat("angle"),
                compound.getBoolean("forced"));
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag root = new CompoundTag();

        ListTag list = new ListTag();
        for (SpawnPoint point : points) {
            list.add(serializeSpawnPoint(point));
        }

        root.put("Points", list);

        if (reorderingTarget != null)
            root.put("ReorderingTarget", serializeSpawnPoint(reorderingTarget));

        root.putBoolean("WorldSpawnAllowed", canChooseWorldSpawn);
        root.putBoolean("RespawnChoiceAllowed", canChooseRespawn);

        return root;
    }

    @Override
    public void loadNbt(CompoundTag nbt) {
        points.clear();
        reorderingTarget = null;
        ListTag list = nbt.getList("Points", Tag.TAG_COMPOUND);

        for (Tag element : list) {
            if (element instanceof CompoundTag compound) {
                points.add(deserializeSpawnPoint(compound));
            }
        }

        if (nbt.contains("ReorderingTarget"))
            reorderingTarget = deserializeSpawnPoint(nbt.getCompound("ReorderingTarget"));

        canChooseWorldSpawn = nbt.getBoolean("WorldSpawnAllowed");
        canChooseRespawn = nbt.getBoolean("RespawnChoiceAllowed");
    }

    public void addPoint(SpawnPoint pos) {
        points.remove(pos);
        points.add(pos);
    }

    public void sortByPrio(MinecraftServer server) {
        points.sort(Comparator.comparingDouble(p -> getBlockPriority(server.getLevel(p.dimension()).getBlockState(p.pos()).getBlock())));
    }

    public SpawnPoint getValidSpawnPoint(ServerPlayer player) {
        SpawnPoint point = getLatestPoint();
        if (point == null) return null;
        else {
            while (!points.isEmpty() && !RespawnAvailability.canRespawnAt(point, player)) {
                removeLatestPoint();
                point = getLatestPoint();
            }
            return point;
        }
    }

    public SpawnPoint getLatestPoint() {
        if (willRespawnAtWorldSpawn) return null;
        return points.isEmpty() ? null : points.get(points.size()-1);
    }

    public void removeLatestPoint() {
        if (!points.isEmpty()) points.remove(points.size() - 1);
    }

    public float getBlockPriority(Block block) {
        for (Map.Entry<TaggableBlock, Float> entry : RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.blockPriorities.entrySet()) {
            if (entry.getKey().matches(block)) return entry.getValue();
        }
        return 0;
    }

    public boolean blockAdditionAllowed(ServerPlayer player, Block block, MinecraftServer server) {
        int overallTotal = RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.overallMaxPoints;
        boolean force = RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.forceSpawnSetting;
        boolean surpassesOverall = overallTotal != -1 && points.size() >= overallTotal;
        if (!force && surpassesOverall) return false;

        TaggableBlock targetType = null;
        int targetAmount = -1;
        for (Map.Entry<TaggableBlock, Integer> entry : RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.maxPointsPerBlock.entrySet()) {
            if (entry.getKey().matches(block)) {
                targetType = entry.getKey();
                targetAmount = entry.getValue();
                break;
            }
        }
        if (targetType == null) {
            targetType = new TaggableBlock(block);
            targetAmount = RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.defaultMaxPoints;
        }

        if (targetAmount == -1) return true;

        SpawnPoint firstMatch = null;
        int collectedAmount = 0;
        for (SpawnPoint point : points) {
            Level world = server.getLevel(point.dimension());
            if (world != null && targetType.matches(world.getBlockState(point.pos()).getBlock())) {
                collectedAmount++;
                if (firstMatch == null) firstMatch = point;
            }
        }

        boolean result = collectedAmount < targetAmount;
        if (force && !result && firstMatch != null) {
            points.remove(firstMatch);
            player.sendSystemMessage(Component.translatable("block.respawnobelisks.override_spawn"));
            collectedAmount--;
        }
        return collectedAmount < targetAmount;
    }

    @Override
    public PlayerSendable createPacket(Entity target) {
        return new SetPriorityChangerPacket(target, this);
    }
}
