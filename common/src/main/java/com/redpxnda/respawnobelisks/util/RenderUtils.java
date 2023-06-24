package com.redpxnda.respawnobelisks.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.redpxnda.respawnobelisks.config.ReviveConfig;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import java.util.List;
import java.util.Random;

import static com.redpxnda.nucleus.util.RenderUtil.*;
import static com.redpxnda.respawnobelisks.registry.ModRegistries.rl;

public class RenderUtils {
    private static ItemStack totemStack = null;
    private static Blaze blaze = null;
    private static TextureAtlasSprite sculkSprite;
    private static TextureAtlasSprite sculkTendrilsSprite;

    public static final Vector3f[] runeCircleColors = {
            new Vector3f(19/255f, 142/255f, 153/255f),
            new Vector3f(41/255f, 223/255f, 235/255f)
    };

    public static int randomInt(Random r, int min, int max) {
        return r.nextInt(max+1-min)+min;
    }
    public static double randomDouble(Random r, double min, double max) {
        return r.nextDouble(max+1-min)+min;
    }

    public static void renderBlaze(RespawnObeliskBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher(); // getting rendering manager and disabling shadows
        renderManager.setRenderShadow(false);

        if (Minecraft.getInstance().level == null || be.getLevel() == null) return;
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
    }

    public static void renderRunes(TextureAtlasSprite sprite, RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        renderRunes(RenderType.translucent(), sprite, new float[]{ 1f, 1f, 1f }, blockEntity, partialTick, poseStack, bufferSource, packedLight);
    }

    public static void renderRunes(RenderType renderType, TextureAtlasSprite sprite, float[] colors, RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        VertexConsumer vc = bufferSource.getBuffer(renderType);

        poseStack.translate(0.5, 18/16f, 0.5);
        for (int i = 0; i < 4; i++) {
            addQuad(false, poseStack.last().pose(), vc, colors[0], colors[1], colors[2], (float) (blockEntity.getClientCharge()/blockEntity.getClientMaxCharge()), 9/32f, 24/32f, -5.505f/16f, 0, sprite.getU(3), sprite.getU(12), sprite.getV(2), sprite.getV(14), packedLight);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        poseStack.popPose();
    }

    public static void renderSculkOverlay(RenderType renderType, RespawnObeliskBlockEntity blockEntity, double charge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (sculkSprite == null) sculkSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(rl("block/sculk_animation"));
        poseStack.pushPose();
        VertexConsumer vc = bufferSource.getBuffer(renderType);

        poseStack.translate(0.5, 18/16f, 0.5);
        for (int i = 0; i < 4; i++) {
            addQuad(false, poseStack.last().pose(), vc, 1f, 1f, 1f, (float) (charge/blockEntity.getClientMaxCharge()), 9/32f, 24/32f, -5.505f/16f, 0, sculkSprite.getU0(), sculkSprite.getU(4.5), sculkSprite.getV0(), sculkSprite.getV(12), packedLight);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        poseStack.popPose();
    }

    public static void renderSculkTendrils(RespawnObeliskBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (sculkTendrilsSprite == null) sculkTendrilsSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(rl("block/tendrils"));
        poseStack.pushPose();
        poseStack.translate(0.5, 34/16f, 0.5);

        VertexConsumer vc = buffer.getBuffer(RenderType.translucent());
        int direction = 0;
        switch (blockEntity.getBlockState().getValue(RespawnObeliskBlock.RESPAWN_SIDE)) {
            case EAST -> direction = 1;
            case SOUTH -> direction = 2;
            case WEST -> direction = 3;
        }
        float size = 0.5f*(5/8f);
        poseStack.mulPose(Axis.YP.rotationDegrees(90*direction));
        addDoubleQuad(poseStack, vc, 1f, 1f, 1f, 1f, size, size, 0, -11.5f/16f, sculkTendrilsSprite.getU0(), sculkTendrilsSprite.getU(10), sculkTendrilsSprite.getV0(), sculkTendrilsSprite.getV(10), light);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        addDoubleQuad(poseStack, vc, 1f, 1f, 1f, 1f, size, size, 0, -11.5f/16f, sculkTendrilsSprite.getU0(), sculkTendrilsSprite.getU(10), sculkTendrilsSprite.getV0(), sculkTendrilsSprite.getV(10), light);

        poseStack.popPose();
    }

    public static void renderRainbow(float progress, float alpha, RespawnObeliskBlockEntity blockEntity, PoseStack poseStack, TextureAtlasSprite sprite, MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        poseStack.translate(0.5, 40/16f, 0.5);

        VertexConsumer vc = buffer.getBuffer(RenderType.translucent());
        int direction = 0;
        switch (blockEntity.getBlockState().getValue(RespawnObeliskBlock.RESPAWN_SIDE)) {
            case EAST -> direction = 1;
            case SOUTH -> direction = 2;
            case WEST -> direction = 3;
        }
        float size = 4;
        poseStack.mulPose(Axis.YP.rotationDegrees(90*direction));
        addQuad((f, bl) -> (bl ? f : 0f)-size/2, (f, bl) -> (bl ? 0f : f)-size/2, poseStack.last().pose(), vc, 1f, 1f, 1f, alpha, size*progress, size, 0, sprite.getU0(), sprite.getU(progress*16f), sprite.getV0(), sprite.getV1(), light);
        addQuad((f, bl) -> 0f-size/2, (f, bl) -> f-size/2, poseStack.last().pose(), vc, 1f, 1f, 1f, alpha, size*progress, size, 0, sprite.getU0(), sprite.getU(progress*16f), sprite.getV1(), sprite.getV0(), light);

        poseStack.popPose();
    }

    public static void renderRuneCircle(long time, float scale, Vector3f[] colors, float alpha, float x, float y, float z, SpriteSet set, Vector3f[] vertices, VertexConsumer vc, int light) {
        rotateVectors(vertices, Axis.XP.rotationDegrees(90));
        TextureAtlasSprite sprite;
        for (int i = 1; i < 5; i++) {
            time*=1 + i/5f;
            if (i % 2 == 0) rotateVectors(vertices, Axis.YP.rotationDegrees(time));
            else rotateVectors(vertices, Axis.YN.rotationDegrees(time));
            scaleVectors(vertices, scale);
            sprite = set.get(i, 4);
            translateVectors(vertices, x, y+(0.01f*i), z);
            addParticleQuad(vertices, vc, colors[i%2].x(), colors[i%2].y(), colors[i%2].z(), alpha, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), light);
            translateVectors(vertices, -x, -y, -z);
            scaleVectors(vertices, 1/scale);
            if (i % 2 == 0) rotateVectors(vertices, Axis.YN.rotationDegrees(time));
            else rotateVectors(vertices, Axis.YP.rotationDegrees(time));
        }
    }

    public static void renderTotemItem(BlockEntityRendererProvider.Context context, RespawnObeliskBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 2.5, 0.5);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        if (blockEntity.getLevel() != null) poseStack.mulPose(Axis.YP.rotationDegrees(blockEntity.getLevel().getGameTime() % 360));
        ItemRenderer itemRenderer = context.getItemRenderer();
        if (totemStack == null) totemStack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(ReviveConfig.revivalItem)));
        itemRenderer.renderStatic(
                totemStack,
                ItemTransforms.TransformType.GROUND,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                1
        );
        poseStack.popPose();
    }
}
