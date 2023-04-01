package com.redpxnda.respawnobelisks.network.handler;

import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.particle.ParticlePack;
import com.redpxnda.respawnobelisks.scheduled.client.ClientRuneCircleTask;
import com.redpxnda.respawnobelisks.scheduled.client.ScheduledClientTasks;
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

@Environment(EnvType.CLIENT)
public class S2CHandlers {
    public static void setupRuneCircleRenderPacket(int time, BlockPos pos) {
        ScheduledClientTasks.schedule(new ClientRuneCircleTask(time, pos));
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
            ParticlePack.firePackMethod(pack.particleHandler, method, level, player, pos);
        else
            ParticlePack.firePackMethod(pack.particleHandler, method, level, null, pos);
    }

    public static void playClientSound(SoundEvent event, float pitch, float volume) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(event, pitch, volume);
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
}
