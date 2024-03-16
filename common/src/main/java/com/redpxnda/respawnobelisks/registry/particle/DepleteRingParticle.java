package com.redpxnda.respawnobelisks.registry.particle;

import com.redpxnda.nucleus.client.Rendering;
import com.redpxnda.nucleus.math.MathUtil;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DepleteRingParticle extends SpriteBillboardParticle {
    private static final MatrixStack poseStack = ChargeIndicatorParticle.poseStack;
    protected float oQuadSize;

    protected DepleteRingParticle(SpriteProvider sprites, ClientWorld clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(clientLevel, x, y, z);
        setSprite(sprites);
        maxAge = 50;
    }

    @Override
    public void tick() {
        super.tick();
        oQuadSize = scale;
        scale+=0.25/(age/4f + 1);
        if (age > 38)
            alpha-=0.05;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float f) {
        Vec3d vec3 = camera.getPos();
        float aX = (float)(MathHelper.lerp(f, this.prevPosX, this.x) - vec3.getX());
        float aY = (float)(MathHelper.lerp(f, this.prevPosY, this.y) - vec3.getY());
        float aZ = (float)(MathHelper.lerp(f, this.prevPosZ, this.z) - vec3.getZ());

        poseStack.push();
        poseStack.translate(aX, aY, aZ);
        poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        float scale = getSize(f);
        int light = this.getBrightness(f);
        Rendering.addDoubleParticleQuad(
                Rendering.QUAD, poseStack, vertexConsumer,
                red, green, blue, alpha,
                scale, scale, scale,
                getMinU(), getMaxU(), getMinV(), getMaxV(),
                light);

        poseStack.pop();
    }

    @Override
    public @NotNull ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getSize(float f) {
        return MathUtil.lerp(f, oQuadSize, scale);
    }

    public static class Provider implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider sprites;

        public Provider(SpriteProvider sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(DefaultParticleType particleOptions, ClientWorld clientLevel, double d, double e, double f, double g, double h, double i) {
            return new DepleteRingParticle(sprites, clientLevel, d, e, f, g, h, i);
        }
    }
}
