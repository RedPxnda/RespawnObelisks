package com.redpxnda.respawnobelisks.network.handler;

import com.redpxnda.respawnobelisks.registry.ModRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class S2CHandlers {
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

    public static void obeliskInteractionPacket1(BlockPos blockPos, String sound, boolean isCurse) {
        Level pLevel = Minecraft.getInstance().level;
        if (pLevel != null) {
            Optional<SoundEvent> soundHolder = Registry.SOUND_EVENT.getOptional(new ResourceLocation(sound));
            soundHolder.ifPresent(soundEventHolder -> pLevel.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), soundEventHolder, SoundSource.BLOCKS, 1, 1, false));
            if (!isCurse) {
                for (int i = 0; i < 900; i += 5) {
                    int i1 = i;
                    if (i1 >= 360) i1 = 0;
                    double radians = i * Math.PI / 180;
                    pLevel.addParticle(ParticleTypes.TOTEM_OF_UNDYING, blockPos.getX() + Math.sin(radians) * 0.5 + 0.5, blockPos.getY() + i / 360f, blockPos.getZ() + Math.cos(radians) * 0.5 + 0.5, Math.sin(radians) / 20, 0, Math.cos(radians) / 20);
                }
            } else {
                for (int i = 0; i < 360; i+=3) {
                    double radians = i * Math.PI / 180;
                    pLevel.addParticle(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.NETHER_WART_BLOCK.defaultBlockState()),
                            blockPos.getX()+0.5,
                            blockPos.getY()+0.65,
                            blockPos.getZ()+0.5,
                            Math.sin(radians) / 1.5,
                            0.25,
                            Math.cos(radians) / 1.5
                    );
                }
            }
        }
    }

    public static void obeliskInteractionPacket2(String sound, BlockPos blockPos, boolean isNegative, boolean isRespawn) {
        Level pLevel = Minecraft.getInstance().level;
        if (pLevel != null) {
            Optional<SoundEvent> soundHolder = Registry.SOUND_EVENT.getOptional(new ResourceLocation(sound));
            soundHolder.ifPresent(soundEventHolder -> pLevel.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), soundEventHolder, SoundSource.BLOCKS, 1, 1, false));
            if (isRespawn) return;
            if (!isNegative) {
                for (int i = 0; i < 360; i += 5) {
                    double radians = i * Math.PI / 180;
                    pLevel.addParticle(ParticleTypes.END_ROD, blockPos.getX() + Math.sin(radians)*3 + 0.5, blockPos.getY() + 0.7, blockPos.getZ() + Math.cos(radians)*3 + 0.5, -Math.sin(radians)/5, 0.05, -Math.cos(radians)/5);
                }
            } else {
                for (int i = 0; i < 360; i += 5) {
                    double radians = i * Math.PI / 180;
                    pLevel.addParticle(ParticleTypes.END_ROD, blockPos.getX() + Math.sin(radians)*0.5 + 0.5, blockPos.getY() + 1.6, blockPos.getZ() + Math.cos(radians)*0.5 + 0.5, Math.sin(radians)/5, -0.05, Math.cos(radians)/5);
                }
            }
        }
    }

    public static void syncEffectsPacket(int amplifier, int duration) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.addEffect(new MobEffectInstance(ModRegistries.IMMORTALITY_CURSE.get(), duration, amplifier));
        }
    }
}
