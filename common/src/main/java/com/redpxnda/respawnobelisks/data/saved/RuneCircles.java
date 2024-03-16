package com.redpxnda.respawnobelisks.data.saved;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class RuneCircles extends PersistentState {
    public final ServerWorld level;
    private final List<RuneCircle> runeCircles = new ArrayList<>();

    public void create(ServerPlayerEntity player, ItemStack stack, BlockPos pos, BlockPos target, double x, double y, double z) {
        runeCircles.add(new RuneCircle(level, player, stack, pos, target, x, y, z));
        this.markDirty();
    }

    public RuneCircles(ServerWorld level) {
        this.level = level;
        this.markDirty();
    }

    public static RuneCircles getCache(ServerWorld level) {
        return level.getPersistentStateManager().getOrCreate(tag -> RuneCircles.load(level, tag), () -> new RuneCircles(level), "rune_circles");
    }

    public static RuneCircles load(ServerWorld level, NbtCompound tag) {
        RuneCircles circles = new RuneCircles(level);
        tag.getList("RuneCircles", 10).forEach(t -> {
            if (t instanceof NbtCompound compoundTag)
                circles.runeCircles.add(RuneCircle.fromNbt(level, compoundTag));
        });

        circles.markDirty();
        return circles;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        NbtList list = new NbtList();
        runeCircles.forEach(runeCircle -> list.add(runeCircle.save(new NbtCompound())));
        tag.put("RuneCircles", list);

        return tag;
    }

    public void tick() {
        Iterator<RuneCircle> iterator = runeCircles.iterator();
        while (iterator.hasNext()) {
            RuneCircle circle = iterator.next();
            circle.tick(level);
            if (circle.stopped) {
                iterator.remove();
                this.markDirty();
            }
        }
    }
}
