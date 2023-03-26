package com.redpxnda.respawnobelisks.registry.particle.packs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class DefaultPack implements IBasicPack {
    public final ParticleOptions particle;

    public DefaultPack(ParticleOptions particle) {
        this.particle = particle;
    }

    @Override
    public void depleteAnimation(Level level, Player player, BlockPos blockPos) {
        for (int i = 0; i < 360; i += 5) {
            double radians = i * Math.PI / 180;
            level.addParticle(particle, blockPos.getX() + Math.sin(radians)*0.5 + 0.5, blockPos.getY() + 1.6, blockPos.getZ() + Math.cos(radians)*0.5 + 0.5, Math.sin(radians)/5, -0.05, Math.cos(radians)/5);
        }
    }

    @Override
    public void chargeAnimation(Level level, Player player, BlockPos blockPos) {
        for (int i = 0; i < 360; i += 5) {
            double radians = i * Math.PI / 180;
            level.addParticle(particle, blockPos.getX() + Math.sin(radians)*3 + 0.5, blockPos.getY() + 0.7, blockPos.getZ() + Math.cos(radians)*3 + 0.5, -Math.sin(radians)/5, 0.05, -Math.cos(radians)/5);
        }
    }
}
