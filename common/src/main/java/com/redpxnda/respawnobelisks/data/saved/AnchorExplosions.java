package com.redpxnda.respawnobelisks.data.saved;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class AnchorExplosions extends PersistentState {
    public final ServerWorld level;
    private final List<AnchorExplosion> anchorExplosions = new ArrayList<>();

    public void create(int tick, int delay, int charge, BlockPos pos) {
        anchorExplosions.add(new AnchorExplosion(tick, delay, charge, pos));
        this.markDirty();
    }

    public AnchorExplosions(ServerWorld level) {
        this.level = level;
        this.markDirty();
    }

    public static AnchorExplosions getCache(ServerWorld level) {
        return level.getPersistentStateManager().getOrCreate(tag -> AnchorExplosions.load(level, tag), () -> new AnchorExplosions(level), "anchor_explosions");
    }

    public static AnchorExplosions load(ServerWorld level, NbtCompound tag) {
        AnchorExplosions explosions = new AnchorExplosions(level);
        tag.getList("Explosions", 10).forEach(t -> {
            if (t instanceof NbtCompound compoundTag)
                explosions.anchorExplosions.add(AnchorExplosion.fromNbt(compoundTag));
        });

        explosions.markDirty();
        return explosions;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        NbtList list = new NbtList();
        anchorExplosions.forEach(explosion -> list.add(explosion.save(new NbtCompound())));
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
                this.markDirty();
            }
        }
    }
}
