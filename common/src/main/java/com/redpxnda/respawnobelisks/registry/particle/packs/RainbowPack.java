package com.redpxnda.respawnobelisks.registry.particle.packs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.particle.ParticlePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.BlockPositionSource;

import static com.redpxnda.respawnobelisks.util.RenderUtils.renderRainbow;

public class RainbowPack extends SimpleRuneColorPack {
    private static TextureAtlasSprite RAINBOW_SPRITE = null;

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
        if (level != null && level.getGameTime()-blockEntity.getLastCharge() <= 100) {
            if (RAINBOW_SPRITE == null) RAINBOW_SPRITE = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ParticlePack.getPackTextures().get("rainbow"));
            float time = level.getGameTime()-blockEntity.getLastCharge();
            renderRainbow(time/100f, blockEntity, stack, RAINBOW_SPRITE, buffer, light);
        }

        return false;
    }

    @Override
    public void chargeAnimation(Level level, Player player, BlockPos blockPos) {
        for (int i = 0; i < 360; i+=72) {
            double rad = i * Math.PI / 180f;
            level.addParticle(
                    new VibrationParticleOption(new BlockPositionSource(blockPos), 20),
                    blockPos.getX() + 0.5 + Math.sin(rad)*3,
                    blockPos.getY() + 1.5,
                    blockPos.getZ() + 0.5 + Math.cos(rad)*3,
                    0,
                    0,
                    0);
        }
    }

    @Override
    public void depleteAnimation(Level level, Player player, BlockPos blockPos) {
        for (int i = 0; i < 60; i+=10) {
            level.addParticle(
                    new ShriekParticleOption(i),
                    blockPos.getX()+0.5,
                    blockPos.getY()+2,
                    blockPos.getZ()+0.5,
                    0,
                    0,
                    0
            );
        }
    }

    @Override
    public SoundEvent depleteSound() {
        return SoundEvents.SCULK_SHRIEKER_SHRIEK;
    }

    @Override
    public SoundEvent chargeSound() {
        return SoundEvents.WARDEN_DEATH;
    }
}
