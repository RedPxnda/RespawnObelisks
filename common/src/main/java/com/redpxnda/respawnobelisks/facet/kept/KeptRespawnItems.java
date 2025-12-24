package com.redpxnda.respawnobelisks.facet.kept;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

public class KeptRespawnItems implements EntityFacet<CompoundTag> {
    public static FacetKey<KeptRespawnItems> KEY;

    public final Map<String, KeptItemsModule> modules = new HashMap<>();

    public KeptRespawnItems(ServerPlayer player) {
        KeptItemsModule.MODULES.forEach((key, creator) -> {
            KeptItemsModule module = creator.apply(player);
            if (module != null) modules.put(key, module);
        });
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag compound = new CompoundTag();
        modules.forEach((key, module) -> compound.put(key, module.toNbt()));
        return compound;
    }

    @Override
    public void loadNbt(CompoundTag nbt) {
        modules.forEach((key, module) -> {
            Tag element = nbt.get(key);
            if (element != null) module.fromNbt(element);
        });
    }

    public void restore(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        modules.forEach((key, module) -> module.restore(oldPlayer, newPlayer));
    }

    public void scatter(double x, double y, double z, ServerPlayer player) {
        modules.forEach((key, module) -> module.scatter(x, y, z, player));
    }

    public boolean gather(ServerPlayer player) {
        if (!isEmpty()) return false;
        modules.forEach((key, module) -> module.gather(player));
        return true;
    }

    public boolean isEmpty() {
        for (Map.Entry<String, KeptItemsModule> entry : modules.entrySet())
            if (!entry.getValue().isEmpty()) return false;
        return true;
    }
}
