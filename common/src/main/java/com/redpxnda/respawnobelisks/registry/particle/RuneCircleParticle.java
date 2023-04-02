package com.redpxnda.respawnobelisks.registry.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.respawnobelisks.util.RenderUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class RuneCircleParticle extends Particle {
    protected RuneCircleParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
        this.lifetime = 100;
        this.setSize(1, 1);
        this.hasPhysics = false;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        RenderUtils.renderRuneCircle(level.getGameTime(), 1f, x, y, z, new PoseStack(), vertexConsumer, LightTexture.FULL_BRIGHT);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet spriteSet) {}

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new RuneCircleParticle(clientLevel, d, e, f);
        }
    }
}
