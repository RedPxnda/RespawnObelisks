package com.redpxnda.respawnobelisks.facet;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import net.minecraft.nbt.ByteTag;

public class HardcoreRespawningTracker implements EntityFacet<ByteTag> {
    public static FacetKey<HardcoreRespawningTracker> KEY;

    public boolean canRespawn = false;

    @Override
    public ByteTag toNbt() {
        return ByteTag.valueOf(canRespawn);
    }

    @Override
    public void loadNbt(ByteTag nbt) {
        canRespawn = nbt.getAsByte() == 1;
    }
}
