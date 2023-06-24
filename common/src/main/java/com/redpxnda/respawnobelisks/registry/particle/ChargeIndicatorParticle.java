package com.redpxnda.respawnobelisks.registry.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.registry.particles.DynamicPoseStackParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;
import static com.redpxnda.respawnobelisks.registry.ModRegistries.rl;

public class ChargeIndicatorParticle extends DynamicPoseStackParticle {
    protected ChargeIndicatorParticle(TextureAtlasSprite sprite, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(p -> {}, p -> {}, (particle, ps, cam) -> {
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());
            vc.vertex(ps.last().pose(), 0f, 0f, 0f).color(1f, 1f, 1f, particle.alpha).normal(1, 0, 0).endVertex();
            vc.vertex(ps.last().pose(), (float) (xSpeed - particle.getX()), (float) (ySpeed - particle.getY()), (float) (zSpeed - particle.getZ())).color(1f, 1f, 1f, particle.alpha).normal(1, 0, 0).endVertex();
            ps.mulPose(cam.rotation());
        }, sprite, RenderType.translucent(), clientLevel, x, y, z, 0, 0, 0);
        this.setLifetime(50);
        this.setFriction(0.95f);
        this.setPhysics(false);
        this.scale = 0.25f;
        this.alpha = 0f;

        this.setXSpeed(-(x-(xSpeed)) / 18f);
        this.setYSpeed(-(y-ySpeed) / 18f);
        this.setZSpeed(-(z-(zSpeed)) / 18f);
    }

    @Override
    public void tick() {
        super.tick();
        if (scale > 0) scale-=0.001;
        if (alpha < 1)
            alpha+=0.1;
    }

    public static class Provider extends DynamicPoseStackParticle.Provider {
        public Provider() {
            super(
                    rl("item/charge_indicator"),
                    RenderType.translucent(),
                    s -> {},
                    t -> {},
                    (p, ps, cam) -> {}
            );
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            if (sprite == null) setupSprite();
            return new ChargeIndicatorParticle(sprite, clientLevel, d, e, f, g, h, i);
        }
    }
}
