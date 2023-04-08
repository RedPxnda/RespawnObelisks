package com.redpxnda.respawnobelisks.data.saved;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AnchorExplosions extends SavedData {
    public final ServerLevel level;
    private final List<AnchorExplosion> anchorExplosions = new ArrayList<>();

    public void create(int tick, int delay, int charge, BlockPos pos) {
        anchorExplosions.add(new AnchorExplosion(tick, delay, charge, pos));
        this.setDirty();
    }

    public AnchorExplosions(ServerLevel level) {
        this.level = level;
        this.setDirty();
    }

    public static AnchorExplosions getCache(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(tag -> AnchorExplosions.load(level, tag), () -> new AnchorExplosions(level), "anchor_explosions");
    }

    public static AnchorExplosions load(ServerLevel level, CompoundTag tag) {
        AnchorExplosions explosions = new AnchorExplosions(level);
        tag.getList("Explosions", 10).forEach(t -> {
            if (t instanceof CompoundTag compoundTag)
                explosions.anchorExplosions.add(AnchorExplosion.fromNbt(compoundTag));
        });

        explosions.setDirty();
        return explosions;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        anchorExplosions.forEach(explosion -> list.add(explosion.save(new CompoundTag())));
        tag.put("Explosions", list);

        return tag;
    }

    public void tick() {
        Iterator<AnchorExplosion> iterator = anchorExplosions.iterator();
        while (iterator.hasNext()) {
            AnchorExplosion explosion = iterator.next();
            explosion.tick(level);
            if (explosion.stopped) {
                iterator.remove();
                this.setDirty();
            }
        }
    }
}
