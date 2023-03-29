package com.redpxnda.respawnobelisks.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.particle.ParticlePack;
import com.redpxnda.respawnobelisks.registry.particle.packs.IBasicPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RenderUtils {
    private static ItemStack TOTEM_STACK = null;

    public static void renderRunes(TextureAtlasSprite sprite, IBasicPack pack, RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.translucent());

        float[] colors = pack.runeColor(partialTick, blockEntity.getLevel());
        poseStack.translate(0.5, 18/16f, 0.5);
        for (int i = 0; i < 4; i++) {
            addQuad(poseStack.last().pose(), vc, colors[0], colors[1], colors[2], (float) (blockEntity.getCharge()/blockEntity.getMaxCharge()), 9/32f, 24/32f, -5.501f/16f, 0, sprite.getU(3), sprite.getU(12), sprite.getV(2), sprite.getV(14), packedLight);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
        }
        poseStack.popPose();
    }

    public static void renderSculkTendrils(RespawnObeliskBlockEntity blockEntity, PoseStack poseStack, TextureAtlasSprite sprite, MultiBufferSource buffer, int light) {
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
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90*direction));
        addDoubleQuad(poseStack, vc, 1f, 1f, 1f, 1f, size, size, 0, -11.5f/16f, sprite.getU(58/8f), sprite.getU(68/8f), sprite.getV(6/8f), sprite.getV(16/8f), light);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180f));
        addFlippedDoubleQuad(poseStack, vc, 1f, 1f, 1f, 1f, size, size, 0, -11.5f/16f, sprite.getU(58/8f), sprite.getU(68/8f), sprite.getV(6/8f), sprite.getV(16/8f), light);

        poseStack.popPose();
    }

    public static void renderRainbow(float progress, RespawnObeliskBlockEntity blockEntity, PoseStack poseStack, TextureAtlasSprite sprite, MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        poseStack.translate(0.5, 40/16f, 0.5);

        VertexConsumer vc = buffer.getBuffer(RenderType.translucent());
        int direction = 0;
        switch (blockEntity.getBlockState().getValue(RespawnObeliskBlock.RESPAWN_SIDE)) {
            case EAST -> direction = 1;
            case SOUTH -> direction = 2;
            case WEST -> direction = 3;
        }
        float size = 2;
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90*direction));
        //poseStack.translate(progress, 0, 0);
        addDoubleQuad(poseStack, vc, 1f, 1f, 1f, 1f, size*progress, size, 0, -progress, sprite.getU0(), sprite.getU(progress*16f), sprite.getV0(), sprite.getV1(), light);

        poseStack.popPose();
    }

    public static void renderTotemItem(BlockEntityRendererProvider.Context context, RespawnObeliskBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 2.5, 0.5);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        if (blockEntity.getLevel() != null) poseStack.mulPose(Vector3f.YP.rotationDegrees(blockEntity.getLevel().getGameTime() % 360));
        ItemRenderer itemRenderer = context.getItemRenderer();
        if (TOTEM_STACK == null) TOTEM_STACK = new ItemStack(Items.TOTEM_OF_UNDYING.arch$holder());
        itemRenderer.renderStatic(
                TOTEM_STACK,
                ItemTransforms.TransformType.GROUND,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                1
        );
        poseStack.popPose();
    }

    public static void renderNameTag(BlockEntityRendererProvider.Context context, boolean renderingTotem, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        poseStack.translate(0.5, renderingTotem ? 3.35 : 2.5, 0.5);
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = poseStack.last().pose();
        float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25f);
        int k = (int)(g * 255.0f) << 24;
        Font font = context.getFont();
        float h = -font.width(component) / 2f;
        //font.drawInBatch(component, h, 0, 0x20FFFFFF, false, matrix4f, multiBufferSource, true, k, i);
        font.drawInBatch(component, h, 0, -1, false, matrix4f, multiBufferSource, false, k, i);
        poseStack.popPose();
    }

    public static void addDoubleQuad(PoseStack stack, VertexConsumer vc, float red, float green, float blue, float alpha, float x, float y, float z, float xOffset, float u0, float u1, float v0, float v1, int light) {
        addQuad(stack.last().pose(), vc, red, green, blue, alpha, x, y, z, xOffset, u0, u1, v0, v1, light);
        stack.scale(-1, 1, 1);
        addReverseQuad(stack.last().pose(), vc, red, green, blue, alpha, -x, y, z, -xOffset, u0, u1, v0, v1, light);
    }

    public static void addFlippedDoubleQuad(PoseStack stack, VertexConsumer vc, float red, float green, float blue, float alpha, float x, float y, float z, float xOffset, float u0, float u1, float v0, float v1, int light) {
        addReverseQuad(stack.last().pose(), vc, red, green, blue, alpha, -x, y, z, -xOffset, u0, u1, v0, v1, light);
        stack.scale(-1, 1, 1);
        addQuad(stack.last().pose(), vc, red, green, blue, alpha, x, y, z, xOffset, u0, u1, v0, v1, light);
    }

    public static void addQuad(Matrix4f matrix4f, VertexConsumer vc, float red, float green, float blue, float alpha, float x, float y, float z, float xOffset, float u0, float u1, float v0, float v1, int light) {
        addVertex(matrix4f, vc, red, green, blue, alpha, x+xOffset, y, z, u0, v0, light);
        addVertex(matrix4f, vc, red, green, blue, alpha, x+xOffset, -y, z, u0, v1, light);
        addVertex(matrix4f, vc, red, green, blue, alpha, -x+xOffset, -y, z, u1, v1, light);
        addVertex(matrix4f, vc, red, green, blue, alpha, -x+xOffset, y, z, u1, v0, light);
    }
    public static void addReverseQuad(Matrix4f matrix4f, VertexConsumer vc, float red, float green, float blue, float alpha, float x, float y, float z, float xOffset, float u0, float u1, float v0, float v1, int light) {
        addVertex(matrix4f, vc, red, green, blue, alpha, -x+xOffset, y, z, u1, v0, light);
        addVertex(matrix4f, vc, red, green, blue, alpha, -x+xOffset, -y, z, u1, v1, light);
        addVertex(matrix4f, vc, red, green, blue, alpha, x+xOffset, -y, z, u0, v1, light);
        addVertex(matrix4f, vc, red, green, blue, alpha, x+xOffset, y, z, u0, v0, light);
    }

    public static void addVertex(Matrix4f matrix4f, VertexConsumer vc, float red, float green, float blue, float alpha, float x, float y, float z, float u, float v, int light) {
        vc.vertex(matrix4f, x, y, z).color(red, green, blue, alpha).uv(u, v).uv2(light).normal(1, 0, 0).endVertex();
    }
}
