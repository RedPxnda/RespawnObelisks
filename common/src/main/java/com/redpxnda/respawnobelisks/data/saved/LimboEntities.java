package com.redpxnda.respawnobelisks.data.saved;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LimboEntities extends PersistentState {
    public final Map<UUID, NbtCompound> limboEntities = new HashMap<>();

    public static LimboEntities getCache(ServerWorld level) {
        return level.getPersistentStateManager().getOrCreate(LimboEntities::load, LimboEntities::new, "limbo_entities");
    }

    public static LimboEntities load(NbtCompound tag) {
        LimboEntities entities = new LimboEntities();
        NbtCompound compound = tag.getCompound("LimboEntities");
        compound.getKeys().forEach(k -> {
            NbtElement element = tag.get(k);
            if (element instanceof NbtCompound compoundTag)
                entities.limboEntities.put(UUID.fromString(k), compoundTag);
        });

        entities.markDirty();
        return entities;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound compound = new NbtCompound();
        limboEntities.forEach((k, v) -> compound.put(k.toString(), v));
        nbt.put("LimboEntities", compound);

        return nbt;
    }
}
