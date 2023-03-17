package com.redpxnda.respawnobelisks.registry.particle;

import com.redpxnda.respawnobelisks.registry.particle.packs.DefaultPack;
import com.redpxnda.respawnobelisks.registry.particle.packs.ExperiencePack;
import com.redpxnda.respawnobelisks.registry.particle.packs.IBasicPack;
import com.redpxnda.respawnobelisks.registry.particle.packs.SculkPack;
import net.minecraft.util.StringRepresentable;

public enum ParticlePack implements StringRepresentable {
    DEFAULT("default", new DefaultPack(), 0),
    EXPERIENCE("experience", new ExperiencePack(), 1),
    SCULK("sculk", new SculkPack(), 2);

    public final String id;
    public final IBasicPack particleHandler;
    public final int requiredTier;

    ParticlePack(String id, IBasicPack particleHandler, int requiredTier) {
        this.id = id;
        this.particleHandler = particleHandler;
        this.requiredTier = requiredTier;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
