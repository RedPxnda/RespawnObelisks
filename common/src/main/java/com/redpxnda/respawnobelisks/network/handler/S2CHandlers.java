package com.redpxnda.respawnobelisks.network.handler;

import com.google.common.collect.ImmutableList;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.particle.RuneCircleParticle;
import com.redpxnda.respawnobelisks.registry.particle.RuneCircleType;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import com.redpxnda.respawnobelisks.util.RenderUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

@Environment(EnvType.CLIENT)
public class S2CHandlers {
    public static void setupRuneCircleRenderPacket(boolean kill, int age, double x, double y, double z) {
        if (MinecraftClient.getInstance().world == null) return;
        List<Double> pos = ImmutableList.of(x, y, z);
        if (ClientUtils.activeRuneParticles.containsKey(pos)) {
            RuneCircleParticle particle = ClientUtils.activeRuneParticles.get(pos);
            particle.setAge(age);
            if (kill || !particle.isAlive() || particle.getAge() >= 100) ClientUtils.activeRuneParticles.remove(pos);
        } else
            MinecraftClient.getInstance().world.addParticle(
                    new RuneCircleType.Options(age, 100, 2, RenderUtils.runeCircleColors[0], RenderUtils.runeCircleColors[1]),
                    x, y, z,
                    0, 0, 0
            );
    }

    public static void respawnAnchorPacket(BlockPos blockPos, int charge, boolean isRun) {
        World pLevel = MinecraftClient.getInstance().world;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (pLevel != null && player != null) {
            pLevel.playSound(player, blockPos, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.BLOCKS, 1f, 0.75f);
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

    public static void playParticleAnimation(String method, int playerId, BlockPos pos) {
        ClientWorld level = MinecraftClient.getInstance().world;
        if (level == null) return;
        switch (method) {
            case "curse", "curseAnimation" -> {
                level.playSound(
                        pos.getX(), pos.getY(), pos.getZ(),
                        Registries.SOUND_EVENT.getOrEmpty(new Identifier(RespawnObelisksConfig.INSTANCE.immortalityCurse.curseSound)).orElse(SoundEvents.UI_BUTTON_CLICK.value()), SoundCategory.BLOCKS,
                        1, 1, false
                );
                for (int i = 0; i < 360; i+=3) {
                    double radians = i * Math.PI / 180;
                    level.addParticle(
                            new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.NETHER_WART_BLOCK.getDefaultState()),
                            pos.getX()+0.5,
                            pos.getY()+0.65,
                            pos.getZ()+0.5,
                            Math.sin(radians) / 1.5,
                            0.25,
                            Math.cos(radians) / 1.5
                    );
                }
            }
            case "totem", "respawn" -> {
                level.playSound(
                        pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.ITEM_TOTEM_USE, SoundCategory.BLOCKS,
                        1, 1, false
                );
                for (int i = 0; i < 900; i += 5) {
                    double radians = i * Math.PI / 180;
                    level.addParticle(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + Math.sin(radians) * 0.5 + 0.5, pos.getY() + i / 360f, pos.getZ() + Math.cos(radians) * 0.5 + 0.5, Math.sin(radians) / 20, 0, Math.cos(radians) / 20);
                }
            }
        }
    }

    public static void playClientSound(SoundEvent event, float pitch, float volume) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.playSound(event, pitch, volume);
        }
    }

    public static void playLocalClientSound(SoundEvent event, float pitch, float volume, double x, double y, double z) {
        ClientWorld level = MinecraftClient.getInstance().world;
        if (level != null) {
            level.playSound(x, y, z, event, SoundCategory.MASTER, pitch, volume, false);
        }
    }

    public static void playerTotemAnimation(Item item) {
        GameRenderer gameRenderer = MinecraftClient.getInstance().gameRenderer;
        if (gameRenderer != null) {
            gameRenderer.showFloatingItem(new ItemStack(item.arch$holder()));
        }
    }

    public static void syncEffectsPacket(int amplifier, int duration) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.addStatusEffect(new StatusEffectInstance(ModRegistries.immortalityCurse.get(), duration, amplifier));
        }
    }
}
