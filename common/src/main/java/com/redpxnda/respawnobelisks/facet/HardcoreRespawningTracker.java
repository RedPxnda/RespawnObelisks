package com.redpxnda.respawnobelisks.facet;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import net.minecraft.nbt.NbtByte;

public class HardcoreRespawningTracker implements EntityFacet<NbtByte> {
    public static FacetKey<HardcoreRespawningTracker> KEY;

    public boolean canRespawn = false;

    @Override
    public NbtByte toNbt() {
        return NbtByte.of(canRespawn);
    }

    @Override
    public void loadNbt(NbtByte nbt) {
        canRespawn = nbt.byteValue() == 1;
    }
}
