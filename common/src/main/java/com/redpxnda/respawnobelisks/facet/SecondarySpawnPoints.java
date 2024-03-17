package com.redpxnda.respawnobelisks.facet;

import com.redpxnda.nucleus.codec.tag.TaggableBlock;
import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import com.redpxnda.nucleus.network.PlayerSendable;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.network.SetPriorityChangerPacket;
import com.redpxnda.respawnobelisks.util.RespawnAvailability;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SecondarySpawnPoints implements EntityFacet<NbtCompound> {
    public static FacetKey<SecondarySpawnPoints> KEY;

    public final List<SpawnPoint> points = new ArrayList<>();
    public @Nullable SpawnPoint reorderingTarget;
    public boolean canChooseRespawn = false;
    public boolean canChooseWorldSpawn = false;
    public boolean willRespawnAtWorldSpawn = false;

    public static NbtCompound serializeSpawnPoint(SpawnPoint point) {
        NbtCompound compound = new NbtCompound();
        compound.putString("Dimension", point.dimension().getValue().toString());
        compound.putInt("x", point.pos().getX());
        compound.putInt("y", point.pos().getY());
        compound.putInt("z", point.pos().getZ());
        compound.putFloat("angle", point.angle());
        compound.putBoolean("forced", point.forced());
        return compound;
    }

    public static SpawnPoint deserializeSpawnPoint(NbtCompound compound) {
        return new SpawnPoint(
                RegistryKey.of(RegistryKeys.WORLD, new Identifier(compound.getString("Dimension"))),
                new BlockPos(
                        compound.getInt("x"),
                        compound.getInt("y"),
                        compound.getInt("z")
                ),
                compound.getFloat("angle"),
                compound.getBoolean("forced"));
    }

    @Override
    public NbtCompound toNbt() {
        NbtCompound root = new NbtCompound();

        NbtList list = new NbtList();
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
    public void loadNbt(NbtCompound nbt) {
        points.clear();
        reorderingTarget = null;
        NbtList list = nbt.getList("Points", NbtElement.COMPOUND_TYPE);

        for (NbtElement element : list) {
            if (element instanceof NbtCompound compound) {
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
        points.sort(Comparator.comparingDouble(p -> getBlockPriority(server.getWorld(p.dimension()).getBlockState(p.pos()).getBlock())));
    }

    public SpawnPoint getValidSpawnPoint(ServerPlayerEntity player) {
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

    public boolean blockAdditionAllowed(Block block, MinecraftServer server) {
        int overallTotal = RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.overallMaxPoints;
        if (overallTotal != -1 && points.size() >= overallTotal) return false;

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

        int collectedAmount = 0;
        for (SpawnPoint point : points) {
            World world = server.getWorld(point.dimension());
            if (world != null && targetType.matches(world.getBlockState(point.pos()).getBlock())) collectedAmount++;
        }

        return collectedAmount < targetAmount;
    }

    @Override
    public PlayerSendable createPacket(Entity target) {
        return new SetPriorityChangerPacket(target, this);
    }
}
