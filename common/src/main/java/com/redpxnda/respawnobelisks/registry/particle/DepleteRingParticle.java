package com.redpxnda.respawnobelisks.registry.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.redpxnda.nucleus.client.Rendering;
import com.redpxnda.nucleus.math.MathUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DepleteRingParticle extends TextureSheetParticle {
    private static final PoseStack poseStack = ChargeIndicatorParticle.poseStack;
    protected float oQuadSize;

    protected DepleteRingParticle(SpriteSet sprites, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(clientLevel, x, y, z);
        pickSprite(sprites);
        lifetime = 50;
    }

    @Override
    public void tick() {
        super.tick();
        oQuadSize = quadSize;
        quadSize+=0.25/(age/4f + 1);
        if (age > 38)
            alpha-=0.05;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        Vec3 vec3 = camera.getPosition();
        float aX = (float)(Mth.lerp(f, this.xo, this.x) - vec3.x());
        float aY = (float)(Mth.lerp(f, this.yo, this.y) - vec3.y());
        float aZ = (float)(Mth.lerp(f, this.zo, this.z) - vec3.z());

        poseStack.pushPose();
        poseStack.translate(aX, aY, aZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        float scale = getQuadSize(f);
        int light = this.getLightColor(f);
        Rendering.addDoubleParticleQuad(
                Rendering.QUAD, poseStack, vertexConsumer,
                rCol, gCol, bCol, alpha,
                scale, scale, scale,
                getU0(), getU1(), getV0(), getV1(),
                light);

        poseStack.popPose();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float f) {
        return MathUtil.lerp(f, oQuadSize, quadSize);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new DepleteRingParticle(sprites, clientLevel, d, e, f, g, h, i);
        }
    }
}
