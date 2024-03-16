package com.redpxnda.respawnobelisks.registry.particle;

import com.redpxnda.nucleus.client.Rendering;
import com.redpxnda.nucleus.math.MathUtil;
import com.redpxnda.respawnobelisks.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChargeIndicatorParticle extends SpriteBillboardParticle {
    static final MatrixStack poseStack = new MatrixStack();
    protected float oQuadSize;
    protected final double targetX;
    protected final double targetY;
    protected final double targetZ;

    protected ChargeIndicatorParticle(SpriteProvider sprites, ClientWorld clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(clientLevel, x, y, z);
        setSprite(sprites);
        maxAge = 50;
        velocityMultiplier = 0.95f;
        collidesWithWorld = false;
        scale = 0.25f;
        oQuadSize = scale;
        alpha = 0f;
        targetX = xSpeed;
        targetY = ySpeed;
        targetZ = zSpeed;

        setVelocity(-(x-(xSpeed)) / 18f, -(y-ySpeed) / 18f, -(z-(zSpeed)) / 18f);
    }

    @Override
    public void tick() {
        super.tick();
        oQuadSize = scale;
        if (scale > 0) {
            scale-=0.001;
        }
        if (alpha < 1)
            alpha+=0.1;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float f) {
        Vec3d vec3 = camera.getPos();
        float aX = (float)(MathHelper.lerp(f, this.prevPosX, this.x) - vec3.getX());
        float aY = (float)(MathHelper.lerp(f, this.prevPosY, this.y) - vec3.getY());
        float aZ = (float)(MathHelper.lerp(f, this.prevPosZ, this.z) - vec3.getZ());

        poseStack.push();
        poseStack.translate(aX, aY, aZ);

        VertexConsumerProvider.Immediate bufferSource = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vc = bufferSource.getBuffer(RenderLayer.getLines());
        vc.vertex(poseStack.peek().getPositionMatrix(), 0, 0, 0).color(1f, 1f, 1f, alpha).normal(1, 0, 0).next();
        vc.vertex(poseStack.peek().getPositionMatrix(), (float) (targetX-x), (float) (targetY-y), (float) (targetZ-z)).color(1f, 1f, 1f, alpha).normal(1, 0, 0).next();

        poseStack.multiply(camera.getRotation());
        float scale = getSize(f);
        int light = this.getBrightness(f);
        Rendering.addQuad(
                Rendering.QUAD, poseStack, bufferSource.getBuffer(RenderUtils.particleTranslucent),
                red, green, blue, alpha,
                scale, scale, scale,
                getMinU(), getMaxU(), getMinV(), getMaxV(),
                light);

        poseStack.pop();
        bufferSource.draw();
    }

    @Override
    public @NotNull ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
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
            return new ChargeIndicatorParticle(sprites, clientLevel, d, e, f, g, h, i);
        }
    }
}
