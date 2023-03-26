package com.redpxnda.respawnobelisks.registry.particle.packs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
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

public class BlazingPack extends DefaultPack implements IBasicPack {
    private static Blaze blaze = null;

    public BlazingPack() {
        super(ParticleTypes.FLAME);
    }

    @Override
    public boolean obeliskRenderTick(RespawnObeliskBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher(); // getting rendering manager and disabling shadows
        renderManager.setRenderShadow(false);

        if (Minecraft.getInstance().level == null || be.getLevel() == null) return false; // setting blaze if non-existent
        if (blaze == null) blaze = new Blaze(EntityType.BLAZE, Minecraft.getInstance().level);

        BlockPos pos = be.getBlockPos(); // setting blaze's pos
        blaze.setPos(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);

        float renderTicks = be.getLevel().getGameTime() + partialTick;

        poseStack.pushPose(); // rendering blaze
        poseStack.translate(0.375D, -0.1F, 0.6125D);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        BlazeRenderer renderer = (BlazeRenderer) renderManager.getRenderer(blaze);
        renderer.getModel().root().getChild("head").visible = false;
        for (int i = 0; i < 4; i++) {
            renderer.getModel().root().getChild("part"+i).visible = false;
        }
        renderManager.render(blaze, 0, 0, 0, 0f, renderTicks, poseStack, buffer, 0xFFFFFF);

        renderManager.setRenderShadow(true); // setting things back
        renderer.getModel().root().getChild("head").visible = true;
        for (int i = 0; i < 4; i++) {
            renderer.getModel().root().getChild("part"+i).visible = true;
        }
        poseStack.popPose();
        return false;
    }

    @Override
    public float[] runeColor(float partialTick, Level level) {
        int time = (int) (level.getGameTime() % 100);
        if (time > 50) time = 50-(time-50);
        return new float[] {1f, Mth.lerp(time/50f, 0, 1), 0f};
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
