package com.redpxnda.respawnobelisks.network.handler;

import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.particle.RuneCircleType;
import com.redpxnda.respawnobelisks.registry.particle.packs.ParticlePack;
import com.redpxnda.respawnobelisks.registry.particle.packs.IBasicPack;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

@Environment(EnvType.CLIENT)
public class S2CHandlers {
    public static void setupRuneCircleRenderPacket(int age, double x, double y, double z) {
        if (Minecraft.getInstance().level == null) return;
        ClientUtils.activeRuneParticles.removeIf(particle -> {
            if (particle.getX() == x && particle.getY() == y && particle.getZ() == z) {
                particle.remove();
                return true;
            }
            return false;
        });
        Minecraft.getInstance().level.addParticle(
                new RuneCircleType.Options(age, 100),
                x, y, z,
                0, 0, 0
        );
    }

    public static void respawnAnchorPacket(BlockPos blockPos, int charge, boolean isRun) {
        Level pLevel = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        if (pLevel != null && player != null) {
            pLevel.playSound(player, blockPos, SoundEvents.NOTE_BLOCK_BELL, SoundSource.BLOCKS, 1f, 0.75f);
            if (!isRun) {
                pLevel.addParticle(ParticleTypes.FLASH, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, 0, 0, 0);
                for (int i = -5; i <= 5; i++) {
                    pLevel.addParticle(ParticleTypes.END_ROD, blockPos.getX() + 0.5, blockPos.getY() + i / 2.5f + 0.5, blockPos.getZ() + 0.5, 0, 0, 0);
                }
            } else {
                for (int i = 0; i < 360; i += 20) {
                    double radians = i * Math.PI / 180;
                    pLevel.addParticle(ParticleTypes.EXPLOSION, blockPos.getX() + Math.sin(radians)*charge*2 + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + Math.cos(radians)*charge*2 + 0.5, 0, 0, 0);
                }
            }
        }
    }

    public static void firePackMethodPacket(String method, int playerId, ParticlePack pack, BlockPos pos) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        if (level.getEntity(playerId) instanceof Player player)
            firePackMethod(pack.particleHandler, method, level, player, pos);
        else
            firePackMethod(pack.particleHandler, method, level, null, pos);
    }

    public static void playClientSound(SoundEvent event, float pitch, float volume) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(event, pitch, volume);
        }
    }

    public static void playLocalClientSound(SoundEvent event, float pitch, float volume, double x, double y, double z) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            level.playLocalSound(x, y, z, event, SoundSource.MASTER, pitch, volume, false);
        }
    }

    public static void playerTotemAnimation(Item item) {
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        if (gameRenderer != null) {
            gameRenderer.displayItemActivation(new ItemStack(item.arch$holder()));
        }
    }

    public static void syncEffectsPacket(int amplifier, int duration) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.addEffect(new MobEffectInstance(ModRegistries.IMMORTALITY_CURSE.get(), duration, amplifier));
        }
    }

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
}
