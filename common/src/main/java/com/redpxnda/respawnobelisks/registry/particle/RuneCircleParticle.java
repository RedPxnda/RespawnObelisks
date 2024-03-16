package com.redpxnda.respawnobelisks.registry.particle;

import com.google.common.collect.ImmutableList;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import com.redpxnda.respawnobelisks.util.RenderUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class RuneCircleParticle extends Particle {
    private final SpriteProvider set;
    private final Vector3f[] colors = new Vector3f[2];
    private final float scale;


    protected RuneCircleParticle(int age, int lifetime, float scale, Vector3f primCol, Vector3f secCol, ClientWorld clientLevel, SpriteProvider set, double d, double e, double f) {
        super(clientLevel, d, e, f);
        this.setAge(age);
        this.maxAge = lifetime;
        this.set = set;
        this.setBoundingBoxSpacing(1, 1);
        this.collidesWithWorld = false;
        this.colors[0] = primCol;
        this.colors[1] = secCol;
        this.scale = scale;
        ClientUtils.activeRuneParticles.put(ImmutableList.of(d, e, f), this);
    }

    public void setAge(int newAge) {
        this.age = newAge;
    }

    public int getAge() {
        return this.age;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float f) {
        Vec3d vec3 = camera.getPos();
        float g = (float)(MathHelper.lerp(f, this.prevPosX, this.x) - vec3.getX());
        float h = (float)(MathHelper.lerp(f, this.prevPosY, this.y) - vec3.getY());
        float i = (float)(MathHelper.lerp(f, this.prevPosZ, this.z) - vec3.getZ());
        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
        float alpha = 1f;
        float life = maxAge;
        if (age/life >= 0.8f) alpha*=(life-age)/(life-life*0.8);
        RenderUtils.renderRuneCircle(world.getTime(), scale, colors, alpha, g, h, i, set, vector3fs, vertexConsumer, LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Provider implements ParticleFactory<RuneCircleType.Options> {
        SpriteProvider set;

        public Provider(SpriteProvider spriteSet) {
            this.set = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(RuneCircleType.Options options, ClientWorld clientLevel, double d, double e, double f, double g, double h, double i) {
            return new RuneCircleParticle(options.time(), options.maxTime(), options.scale(), options.primary(), options.secondary(), clientLevel, set, d, e, f);
        }
    }
}
