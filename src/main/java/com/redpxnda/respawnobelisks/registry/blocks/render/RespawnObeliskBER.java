package com.redpxnda.respawnobelisks.registry.blocks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.redpxnda.respawnobelisks.registry.blocks.RespawnObeliskBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MODID;

public class RespawnObeliskBER implements BlockEntityRenderer<RespawnObeliskBlockEntity> {

    public static final ResourceLocation RUNES = new ResourceLocation(MODID, "block/runes");


    public RespawnObeliskBER(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(RespawnObeliskBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        float[][] translations = new float[][]{
                { 7.5f/16f, 18/16f, 2.4f/16f },
                { 2.4f/16f, 0, 8.5f/16f },
                { -2.7f/16f, 0, 14.5f/16f},
                { -7.9f/16f, 0, 20.5f/16f }
        };

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(RUNES);
        pPoseStack.pushPose();
        VertexConsumer vc = pBufferSource.getBuffer(RenderType.translucent());
        float size = 0.5f;
        for (int i = 0; i < 4; i++) {
            if (i > 0) pPoseStack.translate(-translations[i-1][0], 0, -translations[i-1][2]);
            pPoseStack.translate(translations[i][0], translations[i][1], translations[i][2]);
            if (i > 0) pPoseStack.mulPose(new Quaternion(Vector3f.YP, 90, true));
            Matrix4f matrix4f = pPoseStack.last().pose();
            addQuad(matrix4f, vc, 1F, 1F, 1F, (float) pBlockEntity.getCharge()/100F, size, sprite, pPackedLight);
        }
        pPoseStack.popPose();
    }

    private static void addQuad(Matrix4f matrix4f, VertexConsumer vc, float red, float green, float blue, float alpha, float size, TextureAtlasSprite sprite, int light) {
        addVertex(matrix4f, vc, red, green, blue, alpha, size, size*2, 0, sprite.getU0(), sprite.getV0(), light);
        addVertex(matrix4f, vc, red, green, blue, alpha, size, -size*2, 0, sprite.getU0(), sprite.getV1(), light);
        addVertex(matrix4f, vc, red, green, blue, alpha, -size, -size*2, 0, sprite.getU1(), sprite.getV1(), light);
        addVertex(matrix4f, vc, red, green, blue, alpha, -size, size*2, 0, sprite.getU1(), sprite.getV0(), light);
    }

    private static void addVertex(Matrix4f pPose, VertexConsumer pConsumer, float pRed, float pGreen, float pBlue, float pAlpha, float pX, float pY, float pZ, float pU, float pV, int light) {
        pConsumer.vertex(pPose, pX, pY, pZ).color(pRed, pGreen, pBlue, pAlpha).uv(pU, pV).uv2(light).normal(1, 0, 0).endVertex();
    }
}
