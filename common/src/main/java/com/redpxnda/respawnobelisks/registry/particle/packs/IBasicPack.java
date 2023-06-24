package com.redpxnda.respawnobelisks.registry.particle.packs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.config.ChargeConfig;
import com.redpxnda.respawnobelisks.config.CurseConfig;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.Optional;

public interface IBasicPack {
    default void depleteAnimation(Level level, @Nullable Player player, BlockPos blockPos) {}
    default void chargeAnimation(Level level, @Nullable Player player, BlockPos blockPos) {}
    default void chargeServerHandler(ServerLevel level, ServerPlayer player, BlockPos blockPos) {}
    default void depleteServerHandler(ServerLevel level, ServerPlayer player, BlockPos blockPos) {}
    default void curseServerHandler(ServerLevel level, ServerPlayer player, BlockPos blockPos) {}
    default void curseAnimation(Level level, @Nullable Player player, BlockPos blockPos) {
        for (int i = 0; i < 360; i+=3) {
            double radians = i * Math.PI / 180;
            level.addParticle(
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

    default SoundEvent depleteSound() {
        Optional<SoundEvent> sound = Registry.SOUND_EVENT.getOptional(new ResourceLocation(ChargeConfig.obeliskDepleteSound));
        return sound.orElse(SoundEvents.UI_BUTTON_CLICK);
    }
    default SoundEvent chargeSound() {
        Optional<SoundEvent> sound = Registry.SOUND_EVENT.getOptional(new ResourceLocation(ChargeConfig.obeliskChargeSound));
        return sound.orElse(SoundEvents.UI_BUTTON_CLICK);
    }
    default SoundEvent curseSound() {
        Optional<SoundEvent> sound = Registry.SOUND_EVENT.getOptional(new ResourceLocation(CurseConfig.curseSound));
        return sound.orElse(SoundEvents.UI_BUTTON_CLICK);
    }

    default float depleteSoundVolume() {
        return 1f;
    }
    default float chargeSoundVolume() {
        return 1f;
    }
    default float curseSoundVolume() {
        return 1f;
    }
    default float depleteSoundPitch() {
        return 1f;
    }
    default float chargeSoundPitch() {
        return 1f;
    }
    default float curseSoundPitch() {
        return 1f;
    }

    default float[] runeColor(float partialTick, Level level) {
        return new float[] {1.0f, 1.0f, 1.0f};
    }
    default boolean obeliskRenderTick(RespawnObeliskBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        return false;
    }
}
