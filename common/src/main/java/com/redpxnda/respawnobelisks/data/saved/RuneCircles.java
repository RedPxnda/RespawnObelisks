package com.redpxnda.respawnobelisks.data.saved;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RuneCircles extends SavedData {
    public final ServerLevel level;
    private final List<RuneCircle> runeCircles = new ArrayList<>();

    public void create(ServerPlayer player, ItemStack stack, BlockPos pos, BlockPos target, double x, double y, double z) {
        runeCircles.add(new RuneCircle(level, player, stack, pos, target, x, y, z));
        this.setDirty();
    }

    public RuneCircles(ServerLevel level) {
        this.level = level;
        this.setDirty();
    }

    public static RuneCircles getCache(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(tag -> RuneCircles.load(level, tag), () -> new RuneCircles(level), "rune_circles");
    }

    public static RuneCircles load(ServerLevel level, CompoundTag tag) {
        RuneCircles circles = new RuneCircles(level);
        tag.getList("RuneCircles", 10).forEach(t -> {
            if (t instanceof CompoundTag compoundTag)
                circles.runeCircles.add(RuneCircle.fromNbt(level, compoundTag));
        });

        circles.setDirty();
        return circles;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        runeCircles.forEach(runeCircle -> list.add(runeCircle.save(new CompoundTag())));
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
                this.setDirty();
            }
        }
    }
}
