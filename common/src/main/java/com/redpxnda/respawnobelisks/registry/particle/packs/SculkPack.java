package com.redpxnda.respawnobelisks.registry.particle.packs;

import com.redpxnda.respawnobelisks.mixin.ExperienceOrbAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;

public class SculkPack implements IBasicPack {
    @Override
    public void chargeAnimation(Level level, Player player, BlockPos blockPos) {
        for (int i = 0; i < 360; i+=72) {
            double rad = i * Math.PI / 180f;
            level.addParticle(
                    new VibrationParticleOption(new BlockPositionSource(blockPos), 20),
                    blockPos.getX() + 0.5 + Math.sin(rad)*3,
                    blockPos.getY() + 1.5,
                    blockPos.getZ() + 0.5 + Math.cos(rad)*3,
                    0,
                    0,
                    0);
        }
    }

    @Override
    public void depleteAnimation(Level level, Player player, BlockPos blockPos) {
        for (int i = 0; i < 60; i+=10) {
            level.addParticle(
                    new ShriekParticleOption(i),
                    blockPos.getX()+0.5,
                    blockPos.getY()+2,
                    blockPos.getZ()+0.5,
                    0,
                    0,
                    0
            );
        }
    }

    @Override
    public float[] runeColor(float partialTick, Level level) {
        int time = (int) (level.getGameTime() % 100);
        if (time > 50) time = 50-(time-50);
        float r = 13f, g = 63f, b = 74f;
        r+=21*((time+1)/50f);
        g+=102*((time+1)/50f);
        b+=117*((time+1)/50f);
        return new float[] { r/255f, g/255f, b/255f };
    }

    @Override
    public SoundEvent depleteSound() {
        return SoundEvents.SCULK_SHRIEKER_SHRIEK;
    }

    @Override
    public SoundEvent chargeSound() {
        return SoundEvents.WARDEN_DEATH;
    }
}
