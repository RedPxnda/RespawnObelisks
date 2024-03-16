package com.redpxnda.respawnobelisks.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.Objects;

public final class SpawnPoint {
    private final RegistryKey<World> dimension;
    private final BlockPos pos;
    private final float angle;
    private final boolean forced;

    public SpawnPoint(RegistryKey<World> dimension, BlockPos pos, float angle, boolean forced) {
        this.dimension = dimension;
        this.pos = pos;
        this.angle = angle;
        this.forced = forced;
    }

    public GlobalPos asGlobalPos() {
        return GlobalPos.create(dimension, pos);
    }

    public RegistryKey<World> dimension() {
        return dimension;
    }

    public BlockPos pos() {
        return pos;
    }

    public float angle() {
        return angle;
    }

    public boolean forced() {
        return forced;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SpawnPoint) obj;
        return Objects.equals(this.dimension, that.dimension) &&
                Objects.equals(this.pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, pos);
    }

    @Override
    public String toString() {
        return "SpawnPoint[" +
                "dimension=" + dimension + ", " +
                "pos=" + pos + ", " +
                "angle=" + angle + ", " +
                "forced=" + forced + ']';
    }

}
