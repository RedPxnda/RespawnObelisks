package com.redpxnda.respawnobelisks.facet;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import net.minecraft.nbt.IntTag;

public class LimboReviveTracker implements EntityFacet<IntTag> {
    public static FacetKey<LimboReviveTracker> KEY;

    public int trackers = 0;

    @Override
    public IntTag toNbt() {
        return IntTag.valueOf(trackers);
    }

    @Override
    public void loadNbt(IntTag nbt) {
        trackers = nbt.getAsInt();
    }
}
