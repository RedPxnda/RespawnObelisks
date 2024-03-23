package com.redpxnda.respawnobelisks.facet.kept;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class KeptRespawnItems implements EntityFacet<NbtCompound> {
    public static FacetKey<KeptRespawnItems> KEY;

    public final Map<String, KeptItemsModule> modules = new HashMap<>();

    public KeptRespawnItems(ServerPlayerEntity player) {
        KeptItemsModule.MODULES.forEach((key, creator) -> {
            KeptItemsModule module = creator.apply(player);
            if (module != null) modules.put(key, module);
        });
    }

    @Override
    public NbtCompound toNbt() {
        NbtCompound compound = new NbtCompound();
        modules.forEach((key, module) -> compound.put(key, module.toNbt()));
        return compound;
    }

    @Override
    public void loadNbt(NbtCompound nbt) {
        modules.forEach((key, module) -> {
            NbtElement element = nbt.get(key);
            if (element != null) module.fromNbt(element);
        });
    }

    public void restore(ServerPlayerEntity player) {
        modules.forEach((key, module) -> module.restore(player));
    }

    public void scatter(double x, double y, double z, ServerPlayerEntity player) {
        modules.forEach((key, module) -> module.scatter(x, y, z, player));
    }

    public void gather(ServerPlayerEntity player) {
        modules.forEach((key, module) -> module.gather(player));
    }
}
