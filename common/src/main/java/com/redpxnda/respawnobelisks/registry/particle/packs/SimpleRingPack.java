package com.redpxnda.respawnobelisks.registry.particle.packs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.function.TriFunction;

public class SimpleRingPack implements IBasicPack {
    public final ParticleOptions particle;
    public int increase;
    public int max;
    public TriFunction<Double, Integer, Double, Double> hFunc;
    public TriFunction<Double, Integer, Double, Double> vFunc;
    public TriFunction<Double, Integer, Double, Double> hSpeedFunc;
    public TriFunction<Double, Integer, Double, Double> vSpeedFunc;
    public TriFunction<Double, Integer, Double, Double> hFuncDecrease;
    public TriFunction<Double, Integer, Double, Double> vFuncDecrease;
    public TriFunction<Double, Integer, Double, Double> hSpeedFuncDecrease;
    public TriFunction<Double, Integer, Double, Double> vSpeedFuncDecrease;

    public void setHorizFuncs(TriFunction<Double, Integer, Double, Double> hFunc) {
        this.hFunc = hFunc;
        this.hFuncDecrease = hFunc;
    }

    public void setVertFuncs(TriFunction<Double, Integer, Double, Double> vFunc) {
        this.vFunc = vFunc;
        this.vFuncDecrease = vFunc;
    }

    public void setHorizSpeedFuncs(TriFunction<Double, Integer, Double, Double> hSpeedFunc) {
        this.hSpeedFunc = hSpeedFunc;
        this.hSpeedFuncDecrease = hSpeedFunc;
    }

    public void setVertSpeedFuncs(TriFunction<Double, Integer, Double, Double> vSpeedFunc) {
        this.vSpeedFunc = vSpeedFunc;
        this.vSpeedFuncDecrease = vSpeedFunc;
    }

    public boolean shouldChangeY = true;
    public double chargeRadius = 3;
    public double depleteRadius = 0.5;

    public SimpleRingPack(ParticleOptions particle) {
        this.particle = particle;
        this.increase = 5;
        this.max = 360;
        this.setVertSpeedFuncs((d0, i, d1) -> d0);
        this.setHorizSpeedFuncs((d0, i, d1) -> d0);
        this.setHorizFuncs((d0, i, d1) -> d0);
        this.setVertFuncs((d0, i, d1) -> d0);
    }

    @Override
    public void depleteAnimation(Level level, Player player, BlockPos blockPos) {
        for (int i = 0; i < max; i += increase) {
            double radians = i * Math.PI / 180;

            double x = hFuncDecrease.apply(blockPos.getX() + Math.sin(radians)*depleteRadius + 0.5, i, radians);
            double y = vFuncDecrease.apply(blockPos.getY() + (shouldChangeY ? 1.6 : 0), i, radians);
            double z = hFuncDecrease.apply(blockPos.getZ() + Math.cos(radians)*depleteRadius + 0.5, i, radians);

            double xSpeed = hSpeedFuncDecrease.apply(Math.sin(radians)/5, i, radians);
            double ySpeed = vSpeedFuncDecrease.apply(-0.05, i, radians);
            double zSpeed = hSpeedFuncDecrease.apply(Math.cos(radians)/5, i, radians);

            level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    @Override
    public void chargeAnimation(Level level, Player player, BlockPos blockPos) {
        for (int i = 0; i < max; i += increase) {
            double radians = i * Math.PI / 180;

            double x = hFunc.apply(blockPos.getX() + Math.sin(radians)*chargeRadius + 0.5, i, radians);
            double y = vFunc.apply(blockPos.getY() + (shouldChangeY ? 0.7 : 0), i, radians);
            double z = hFunc.apply(blockPos.getZ() + Math.cos(radians)*chargeRadius + 0.5, i, radians);

            double xSpeed = hSpeedFunc.apply(-Math.sin(radians)/5, i, radians);
            double ySpeed = vSpeedFunc.apply(0.05, i, radians);
            double zSpeed = hSpeedFunc.apply(-Math.cos(radians)/5, i, radians);

            level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
