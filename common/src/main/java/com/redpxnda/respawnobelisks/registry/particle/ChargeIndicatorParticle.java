package com.redpxnda.respawnobelisks.registry.particle;

import com.mojang.math.Vector3f;
import com.redpxnda.nucleus.registry.particles.DynamicCameraLockedParticle;
import com.redpxnda.nucleus.registry.particles.DynamicParticle;
import com.redpxnda.respawnobelisks.util.RenderUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChargeIndicatorParticle extends DynamicCameraLockedParticle {
    public final BlockPos pos;

    protected ChargeIndicatorParticle(Consumer<DynamicParticle> onSetup, Consumer<DynamicParticle> onTick, BiConsumer<DynamicParticle, Vector3f[]> onRender, SpriteSet set, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(onSetup, onTick, onRender, set, clientLevel, x, y, z, 0, 0, 0);
        this.setLifetime(50);
        this.setFriction(0.95f);
        this.setPhysics(false);
        this.scale = 0.1f;
        this.alpha = 0f;

        this.pos = new BlockPos(xSpeed, ySpeed, zSpeed);
        this.setXSpeed(-(x-(xSpeed+0.5)) / 18f);
        this.setZSpeed(-(z-(zSpeed+0.5)) / 18f);
        RenderUtils.CHARGE_PARTICLES.put(pos, this);
    }

    public double getX() {
        return xo;
    }
    public double getY() {
        return yo;
    }
    public double getZ() {
        return zo;
    }

    @Override
    public void tick() {
        super.tick();
        scale-=0.001;
        if (alpha < 1)
            alpha+=0.1;
    }

    @Override
    public void remove() {
        super.remove();
        RenderUtils.CHARGE_PARTICLES.remove(pos, this);
    }

    public static class Provider extends DynamicParticle.Provider {
        public Provider(SpriteSet set) {
            super(set);
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new ChargeIndicatorParticle(onSetup, onTick, onRender, set, clientLevel, d, e, f, g, h, i);
        }
    }
}
