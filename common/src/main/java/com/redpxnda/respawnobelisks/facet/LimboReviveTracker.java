package com.redpxnda.respawnobelisks.facet;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import net.minecraft.nbt.NbtInt;

public class LimboReviveTracker implements EntityFacet<NbtInt> {
    public static FacetKey<LimboReviveTracker> KEY;

    public int trackers = 0;

    @Override
    public NbtInt toNbt() {
        return NbtInt.of(trackers);
    }

    @Override
    public void loadNbt(NbtInt nbt) {
        trackers = nbt.intValue();
    }
}
