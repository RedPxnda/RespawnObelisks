package com.redpxnda.respawnobelisks.registry.particle.packs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;

public class BlazingPack extends SimpleRingPack {
    private static Blaze blaze = null;
    private final SimpleRuneColorPack runePack;

    public BlazingPack() {
        super(ParticleTypes.FLAME);

        this.runePack = new SimpleRuneColorPack();
        runePack.ticks = 100;
        runePack.colors.add(new float[] { 255, 50, 0 });
        runePack.colors.add(new float[] { 255, 175, 0 });

        this.increase = 10;
        this.max = 1800;
        this.setVertFuncs((orig, i, rad) -> orig+0.5 + ((i/360) / 3f));
        this.setVertSpeedFuncs((orig, i, rad) -> 0d);
        this.chargeRadius = 4;
        this.shouldChangeY = false;
    }

    @Override
    public boolean obeliskRenderTick(RespawnObeliskBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher(); // getting rendering manager and disabling shadows
        renderManager.setRenderShadow(false);

        if (Minecraft.getInstance().level == null || be.getLevel() == null) return false;
        if (blaze == null) blaze = new Blaze(EntityType.BLAZE, Minecraft.getInstance().level); // setting blaze if non-existent

        BlockPos pos = be.getBlockPos(); // setting blaze's pos
        blaze.setPos(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);

        float renderTicks = be.getLevel().getGameTime() + partialTick;

        poseStack.pushPose(); // rendering blaze
        poseStack.translate(0.375D, -0.65F, 0.6125D);
        poseStack.scale(1.4f, 1.4f, 1.4f);
        BlazeRenderer renderer = (BlazeRenderer) renderManager.getRenderer(blaze);
        renderer.getModel().root().getChild("head").visible = false;
        for (int i = 4; i < 12; i++) {
            renderer.getModel().root().getChild("part"+i).visible = false;
        }
        renderManager.render(blaze, 0, 0, 0, 0f, renderTicks, poseStack, buffer, 0xFFFFFF);

        renderManager.setRenderShadow(true); // setting things back
        renderer.getModel().root().getChild("head").visible = true;
        for (int i = 4; i < 12; i++) {
            renderer.getModel().root().getChild("part"+i).visible = true;
        }
        poseStack.popPose();
        return false;
    }

    @Override
    public float[] runeColor(float partialTick, Level level) {
        return runePack.runeColor(partialTick, level);
    }

    @Override
    public SoundEvent depleteSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    public SoundEvent chargeSound() {
        return SoundEvents.BLAZE_HURT;
    }
}
