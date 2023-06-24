package com.redpxnda.respawnobelisks.registry.particle.packs;

import com.redpxnda.respawnobelisks.mixin.ExperienceOrbAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ExperiencePack implements IBasicPack {
    @Override
    public void chargeServerHandler(ServerLevel level, ServerPlayer player, BlockPos blockPos) {
        for (int i = 0; i < 360; i+=20) {
            double rad = i * Math.PI / 180f;
            ExperienceOrb orb = new ExperienceOrb(level, blockPos.getX() + Math.sin(rad)*0.5 + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + Math.sin(rad)*0.5 + 0.5, 0);
            ((ExperienceOrbAccessor) orb).setAge(5900);
            orb.setDeltaMovement(Math.sin(rad)/10, 0, Math.cos(rad)/10);
            level.addFreshEntity(orb);
        }
    }

    @Override
    public void depleteAnimation(Level level, Player player, BlockPos blockPos) {
        level.addParticle(ParticleTypes.FLASH, blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5, 0, 0, 0);
        for (int i = 0; i < 360; i+=20) {
            double rad = i * Math.PI / 180f;
            level.addParticle(ParticleTypes.FIREWORK, blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5, Math.sin(rad)/3, 0, Math.cos(rad)/3);
        }
    }

    @Override
    public void chargeAnimation(Level level, Player player, BlockPos blockPos) {
        level.addParticle(ParticleTypes.FLASH, blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5, 0, 0, 0);
        for (int i = 0; i < 360; i+=20) {
            double rad = i * Math.PI / 180f;
            level.addParticle(ParticleTypes.FIREWORK, blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5, Math.sin(rad)/3, 0, Math.cos(rad)/3);
        }
    }

    @Override
    public float[] runeColor(float partialTick, Level level) {
        int time = (int) (level.getGameTime() % 100);
        if (time > 50) time = 50-(time-50);
        return new float[] {Mth.lerp(time/50f, 0, 1), 1f, 0f};
    }

    @Override
    public SoundEvent depleteSound() {
        return SoundEvents.GRINDSTONE_USE;
    }

    @Override
    public SoundEvent chargeSound() {
        return SoundEvents.PLAYER_LEVELUP;
    }
}
