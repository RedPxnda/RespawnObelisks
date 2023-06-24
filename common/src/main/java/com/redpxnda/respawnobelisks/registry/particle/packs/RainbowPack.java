package com.redpxnda.respawnobelisks.registry.particle.packs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import com.redpxnda.respawnobelisks.util.RenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import static com.redpxnda.respawnobelisks.util.RenderUtils.renderRainbow;

public class RainbowPack extends SimpleRuneColorPack {
    public RainbowPack() {
        this.ticks = 200;
        this.colors.add(new float[] { 255, 0, 0 });
        this.colors.add(new float[] { 255, 255, 0 });
        this.colors.add(new float[] { 0, 255, 0 });
        this.colors.add(new float[] { 0, 255, 255 });
        this.colors.add(new float[] { 0, 0, 255 });
        this.colors.add(new float[] { 255, 0, 255 });
    }

    @Override
    public boolean obeliskRenderTick(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        Level level = blockEntity.getLevel();
        if (level != null && level.getGameTime()-blockEntity.getLastCharge() <= 60) {
            int time = (int) (level.getGameTime()-blockEntity.getLastCharge());
            if (time <= 50) renderRainbow(time/50f, 1f, blockEntity, stack, RenderUtils.getAtlasSprite("rainbow"), buffer, light);
            else renderRainbow(1, (60-time)/10f, blockEntity, stack, RenderUtils.getAtlasSprite("rainbow"), buffer, light);
            if (time % 5 == 0 && time < 50) {
                if (!ClientUtils.hasTracker("rainbow_pack_sound_ticker") || ClientUtils.getTracker("rainbow_pack_sound_ticker") != time) {
                    BlockPos pos = blockEntity.getBlockPos();
                    level.playLocalSound(
                            pos.getX(), pos.getY(), pos.getZ(),
                            SoundEvents.NOTE_BLOCK_CHIME,
                            SoundSource.BLOCKS,
                            1f, time/50f + 0.5f,
                            false
                    );
                }
                ClientUtils.setTracker("rainbow_pack_sound_ticker", time);
            }
        }

        return false;
    }

    @Override
    public void depleteAnimation(Level level, Player player, BlockPos blockPos) {
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 360; i+=12) {
                double rad = i * Math.PI / 180f;
                level.addParticle(
                        ParticleTypes.CLOUD,
                        blockPos.getX() + 0.5 + Math.sin(rad)*j/5,
                        blockPos.getY() + 2.5,
                        blockPos.getZ() + 0.5 + Math.cos(rad)*j/5,
                        0,
                        0,
                        0);
                if (i % 36 == 0)
                    level.addParticle(
                            ParticleTypes.DRIPPING_WATER,
                            blockPos.getX() + 0.5 + Math.sin(rad)*j/5,
                            blockPos.getY() + 2.49,
                            blockPos.getZ() + 0.5 + Math.cos(rad)*j/5,
                            0,
                            0,
                            0);
            }
        }
    }

    @Override
    public SoundEvent depleteSound() {
        return SoundEvents.AMETHYST_BLOCK_BREAK;
    }

    @Override
    public SoundEvent chargeSound() {
        return null;
    }
}
