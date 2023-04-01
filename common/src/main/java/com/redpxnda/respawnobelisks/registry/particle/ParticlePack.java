package com.redpxnda.respawnobelisks.registry.particle;

import com.redpxnda.respawnobelisks.registry.particle.packs.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public enum ParticlePack implements StringRepresentable {
    DEFAULT("default", new SimpleRingPack(ParticleTypes.END_ROD), 0),
    EXPERIENCE("experience", new ExperiencePack(), 1),
    SCULK("sculk", new SculkPack(), 2),
    BLAZING("blazing", new BlazingPack(), 2),
    RAINBOW("rainbow", new RainbowPack(), 2);

    public static void firePackMethod(IBasicPack pack, String method, Level level, @Nullable Player player, BlockPos pos) {
        SoundEvent event = null;
        float volume = 1f;
        float pitch = 1f;
        switch (method) {
            case "deplete", "depleteAnimation" -> {
                pack.depleteAnimation(level, player, pos);
                event = pack.depleteSound();
                volume = pack.depleteSoundVolume();
                pitch = pack.depleteSoundPitch();
            }
            case "charge", "chargeAnimation" -> {
                pack.chargeAnimation(level, player, pos);
                event = pack.chargeSound();
                volume = pack.chargeSoundVolume();
                pitch = pack.chargeSoundPitch();
            }
            case "curse", "curseAnimation" -> {
                pack.curseAnimation(level, player, pos);
                event = pack.curseSound();
                volume = pack.curseSoundVolume();
                pitch = pack.curseSoundPitch();
            }
            case "totem", "respawn" -> {
                event = SoundEvents.TOTEM_USE;
                for (int i = 0; i < 900; i += 5) {
                    double radians = i * Math.PI / 180;
                    level.addParticle(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + Math.sin(radians) * 0.5 + 0.5, pos.getY() + i / 360f, pos.getZ() + Math.cos(radians) * 0.5 + 0.5, Math.sin(radians) / 20, 0, Math.cos(radians) / 20);
                }
            }
        }
        if (event != null)
            level.playLocalSound(
                    pos.getX(), pos.getY(), pos.getZ(),
                    event, SoundSource.BLOCKS,
                    volume, pitch, false
            );
    }

    public final String id;
    public final IBasicPack particleHandler;
    public final int requiredTier;

    ParticlePack(String id, IBasicPack particleHandler, int requiredTier) {
        this.id = id;
        this.particleHandler = particleHandler;
        this.requiredTier = requiredTier;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
