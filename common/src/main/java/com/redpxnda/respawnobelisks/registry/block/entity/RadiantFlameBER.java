package com.redpxnda.respawnobelisks.registry.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.redpxnda.nucleus.client.Rendering;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.redpxnda.nucleus.client.Rendering.addVertex;
import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class RadiantFlameBER implements BlockEntityRenderer<RadiantFlameBlockEntity> {
    public static final ResourceLocation FLAME = new ResourceLocation(MOD_ID, "block/radiant_flame");
    public static TextureAtlasSprite SPRITE = null;
    private final BlockEntityRendererProvider.Context context;

    public RadiantFlameBER(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(RadiantFlameBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0f, -0.5f, 0f);
        int totalSeconds = blockEntity.timeRemaining/20;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        Component nameTagText = (RespawnObelisksConfig.INSTANCE != null && RespawnObelisksConfig.INSTANCE.radiantFlame.lifetime <= -1) ? Component.nullToEmpty("∞") : Component.nullToEmpty(String.format("%02d:%02d", minutes, seconds));
        Rendering.renderNameTag(context, false, nameTagText, poseStack, bufferSource, packedLight);
        poseStack.popPose();

        if (SPRITE == null) SPRITE = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(FLAME);
        VertexConsumer vc = bufferSource.getBuffer(RenderType.cutout());

        int frame = 1 + (int) (Rendering.getGameTime() % 10)/2;
        float size = (float) (blockEntity.getCharge()/blockEntity.getInitialCharge());

        poseStack.translate(0.5f, (1 + Math.sin(Rendering.getGameAndPartialTime()/8))/4, 0.5f);

        Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.mulPose(Axis.YN.rotationDegrees(cam.getYRot())); // making it always face camera(but only on y axis)

        poseStack.scale(size, size, size);

        // first side
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, -10/16f, 0, 0, SPRITE.getU0(), SPRITE.getV(16*frame/5f), LightTexture.FULL_BRIGHT);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, -10/16f, 20/16f, 0, SPRITE.getU0(), SPRITE.getV(16*(frame-1)/5f), LightTexture.FULL_BRIGHT);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, 10/16f, 20/16f, 0, SPRITE.getU1(), SPRITE.getV(16*(frame-1)/5f), LightTexture.FULL_BRIGHT);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, 10/16f, 0, 0, SPRITE.getU1(), SPRITE.getV(16*frame/5f), LightTexture.FULL_BRIGHT);

        // second side
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, -10/16f, 20/16f, 0, SPRITE.getU0(), SPRITE.getV(16*(frame-1)/5f), LightTexture.FULL_BRIGHT);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, -10/16f, 0, 0, SPRITE.getU0(), SPRITE.getV(16*frame/5f), LightTexture.FULL_BRIGHT);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, 10/16f, 0, 0, SPRITE.getU1(), SPRITE.getV(16*frame/5f), LightTexture.FULL_BRIGHT);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, 10/16f, 20/16f, 0, SPRITE.getU1(), SPRITE.getV(16*(frame-1)/5f), LightTexture.FULL_BRIGHT);
    }
}
