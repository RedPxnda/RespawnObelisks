package com.redpxnda.respawnobelisks.registry.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.client.Rendering;
import com.redpxnda.nucleus.math.MathUtil;
import com.redpxnda.respawnobelisks.util.RenderUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChargeIndicatorParticle extends TextureSheetParticle {
    static final PoseStack poseStack = new PoseStack();
    protected float oQuadSize;
    protected final double targetX;
    protected final double targetY;
    protected final double targetZ;

    protected ChargeIndicatorParticle(SpriteSet sprites, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(clientLevel, x, y, z);
        pickSprite(sprites);
        lifetime = 50;
        friction = 0.95f;
        hasPhysics = false;
        quadSize = 0.25f;
        oQuadSize = quadSize;
        alpha = 0f;
        targetX = xSpeed;
        targetY = ySpeed;
        targetZ = zSpeed;

        setParticleSpeed(-(x-(xSpeed)) / 18f, -(y-ySpeed) / 18f, -(z-(zSpeed)) / 18f);
    }

    @Override
    public void tick() {
        super.tick();
        oQuadSize = quadSize;
        if (quadSize > 0) {
            quadSize-=0.001;
        }
        if (alpha < 1)
            alpha+=0.1;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        Vec3 vec3 = camera.getPosition();
        float aX = (float)(Mth.lerp(f, this.xo, this.x) - vec3.x());
        float aY = (float)(Mth.lerp(f, this.yo, this.y) - vec3.y());
        float aZ = (float)(Mth.lerp(f, this.zo, this.z) - vec3.z());

        poseStack.pushPose();
        poseStack.translate(aX, aY, aZ);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());
        vc.vertex(poseStack.last().pose(), 0, 0, 0).color(1f, 1f, 1f, alpha).normal(1, 0, 0).endVertex();
        vc.vertex(poseStack.last().pose(), (float) (targetX-x), (float) (targetY-y), (float) (targetZ-z)).color(1f, 1f, 1f, alpha).normal(1, 0, 0).endVertex();

        poseStack.mulPose(camera.rotation());
        float scale = getQuadSize(f);
        int light = this.getLightColor(f);
        Rendering.addQuad(
                Rendering.QUAD, poseStack, bufferSource.getBuffer(RenderUtils.particleTranslucent),
                rCol, gCol, bCol, alpha,
                scale, scale, scale,
                getU0(), getU1(), getV0(), getV1(),
                light);

        poseStack.popPose();
        bufferSource.endBatch();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
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
            return new ChargeIndicatorParticle(sprites, clientLevel, d, e, f, g, h, i);
        }
    }
}
