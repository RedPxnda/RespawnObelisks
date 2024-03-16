package com.redpxnda.respawnobelisks.facet;

import com.redpxnda.nucleus.codec.tag.TaggableBlock;
import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SecondarySpawnPoints implements EntityFacet<NbtList> {
    public static FacetKey<SecondarySpawnPoints> KEY;

    public final List<SpawnPoint> points = new ArrayList<>();
    public SpawnPoint reorderingTarget;

    @Override
    public NbtList toNbt() {
        NbtList list = new NbtList();
        for (SpawnPoint point : points) {
            NbtCompound compound = new NbtCompound();
            compound.putString("Dimension", point.dimension().getValue().toString());
            compound.putInt("x", point.pos().getX());
            compound.putInt("y", point.pos().getY());
            compound.putInt("z", point.pos().getZ());
            compound.putFloat("angle", point.angle());
            compound.putBoolean("forced", point.forced());
            list.add(compound);
        }
        return list;
    }

    @Override
    public void loadNbt(NbtList nbt) {
        for (NbtElement element : nbt) {
            if (element instanceof NbtCompound compound) {
                points.add(new SpawnPoint(
                        RegistryKey.of(RegistryKeys.WORLD, new Identifier(compound.getString("Dimension"))),
                        new BlockPos(
                                compound.getInt("x"),
                                compound.getInt("y"),
                                compound.getInt("z")
                        ),
                        compound.getFloat("angle"),
                        compound.getBoolean("forced")));
            }
        }
    }

    public void addPoint(SpawnPoint pos) {
        points.remove(pos);
        points.add(pos);
    }

    public SpawnPoint getLatestPoint() {
        return points.isEmpty() ? null : points.get(points.size()-1);
    }

    public SpawnPoint removeLatestPoint() {
        return points.isEmpty() ? null : points.remove(points.size()-1);
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
}
