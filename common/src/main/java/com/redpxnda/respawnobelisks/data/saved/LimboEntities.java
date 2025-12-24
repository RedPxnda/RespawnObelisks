package com.redpxnda.respawnobelisks.data.saved;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class LimboEntities extends SavedData {
    public final Map<UUID, CompoundTag> limboEntities = new HashMap<>();

    public static LimboEntities getCache(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(LimboEntities::load, LimboEntities::new, "limbo_entities");
    }

    public static LimboEntities load(CompoundTag tag) {
        LimboEntities entities = new LimboEntities();
        CompoundTag compound = tag.getCompound("LimboEntities");
        compound.getAllKeys().forEach(k -> {
            Tag element = tag.get(k);
            if (element instanceof CompoundTag compoundTag)
                entities.limboEntities.put(UUID.fromString(k), compoundTag);
        });

        entities.setDirty();
        return entities;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        CompoundTag compound = new CompoundTag();
        limboEntities.forEach((k, v) -> compound.put(k.toString(), v));
        nbt.put("LimboEntities", compound);

        return nbt;
    }
}
