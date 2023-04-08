package com.redpxnda.respawnobelisks.registry.particle.packs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.RenderUtils;
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
import static com.redpxnda.respawnobelisks.util.RenderUtils.*;

public class SculkPack extends SimpleRuneColorPack {
    private TextureAtlasSprite TENDRILS_SPRITE = null;

    public SculkPack() {
        this.ticks = 100;
        this.colors.add(new float[] { 13f, 63f, 74f });
        this.colors.add(new float[] { 28f, 133f, 156f });
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
    public boolean obeliskRenderTick(RespawnObeliskBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (TENDRILS_SPRITE == null) TENDRILS_SPRITE = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(RenderUtils.getPackTextures().get("sculk_tendrils"));
        renderSculkTendrils(be, poseStack, TENDRILS_SPRITE, buffer, light);
        return false;
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
