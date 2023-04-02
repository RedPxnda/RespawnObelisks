package com.redpxnda.respawnobelisks.registry.particle;

import com.redpxnda.respawnobelisks.registry.particle.packs.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public enum ParticlePack implements StringRepresentable {
    DEFAULT("default", new SimpleRingPack(ParticleTypes.END_ROD), 0),
    EXPERIENCE("experience", new ExperiencePack(), 1),
    SCULK("sculk", new SculkPack(), 2),
    BLAZING("blazing", new BlazingPack(), 2),
    RAINBOW("rainbow", new RainbowPack(), 2);

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
