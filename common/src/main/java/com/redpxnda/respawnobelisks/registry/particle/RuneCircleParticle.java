package com.redpxnda.respawnobelisks.registry.particle;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.respawnobelisks.util.ClientUtils;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class RuneCircleParticle extends Particle {
    private final SpriteSet set;
    private final Vector3f[] colors = new Vector3f[2];
    private final float scale;


    protected RuneCircleParticle(int age, int lifetime, float scale, Vector3f primCol, Vector3f secCol, ClientLevel clientLevel, SpriteSet set, double d, double e, double f) {
        super(clientLevel, d, e, f);
        this.setAge(age);
        this.lifetime = lifetime;
        this.set = set;
        this.setSize(1, 1);
        this.hasPhysics = false;
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
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        Vec3 vec3 = camera.getPosition();
        float g = (float)(Mth.lerp(f, this.xo, this.x) - vec3.x());
        float h = (float)(Mth.lerp(f, this.yo, this.y) - vec3.y());
        float i = (float)(Mth.lerp(f, this.zo, this.z) - vec3.z());
        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
        float alpha = 1f;
        float life = lifetime;
        if (age/life >= 0.8f) alpha*=(life-age)/(life-life*0.8);
        RenderUtils.renderRuneCircle(level.getGameTime(), scale, colors, alpha, g, h, i, set, vector3fs, vertexConsumer, LightTexture.FULL_BRIGHT);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Provider implements ParticleProvider<RuneCircleType.Options> {
        SpriteSet set;

        public Provider(SpriteSet spriteSet) {
            this.set = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(RuneCircleType.Options options, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new RuneCircleParticle(options.time(), options.maxTime(), options.scale(), options.primary(), options.secondary(), clientLevel, set, d, e, f);
        }
    }
}
