package com.redpxnda.respawnobelisks.util;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class SpawnPoint {
    private final ResourceKey<Level> dimension;
    private final BlockPos pos;
    private final float angle;
    private final boolean forced;

    public SpawnPoint(ResourceKey<Level> dimension, BlockPos pos, float angle, boolean forced) {
        this.dimension = dimension;
        this.pos = pos;
        this.angle = angle;
        this.forced = forced;
    }

    public GlobalPos asGlobalPos() {
        return GlobalPos.of(dimension, pos);
    }

    public ResourceKey<Level> dimension() {
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
