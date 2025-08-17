package com.redpxnda.respawnobelisks.registry.block.entity;

import com.redpxnda.nucleus.client.Rendering;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import static com.redpxnda.nucleus.client.Rendering.addVertex;
import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class RadiantFlameBER implements BlockEntityRenderer<RadiantFlameBlockEntity> {
    public static final Identifier FLAME = new Identifier(MOD_ID, "block/radiant_flame");
    public static Sprite SPRITE = null;
    private final BlockEntityRendererFactory.Context context;

    public RadiantFlameBER(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(RadiantFlameBlockEntity blockEntity, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        poseStack.push();
        poseStack.translate(0f, -0.5f, 0f);
        int totalSeconds = blockEntity.timeRemaining/20;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        Text nameTagText = (RespawnObelisksConfig.INSTANCE != null && RespawnObelisksConfig.INSTANCE.radiantFlame.lifetime <= -1) ? Text.of("∞") : Text.of(String.format("%02d:%02d", minutes, seconds));
        Rendering.renderNameTag(context, false, nameTagText, poseStack, bufferSource, packedLight);
        poseStack.pop();

        if (SPRITE == null) SPRITE = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(FLAME);
        VertexConsumer vc = bufferSource.getBuffer(RenderLayer.getCutout());

        int frame = 1 + (int) (Rendering.getGameTime() % 10)/2;
        float size = (float) (blockEntity.getCharge()/blockEntity.getInitialCharge());

        poseStack.translate(0.5f, (1 + Math.sin(Rendering.getGameAndPartialTime()/8))/4, 0.5f);

        Camera cam = MinecraftClient.getInstance().gameRenderer.getCamera();
        poseStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(cam.getYaw())); // making it always face camera(but only on y axis)

        poseStack.scale(size, size, size);

        // first side
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, -10/16f, 0, 0, SPRITE.getMinU(), SPRITE.getFrameV(16*frame/5f), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, -10/16f, 20/16f, 0, SPRITE.getMinU(), SPRITE.getFrameV(16*(frame-1)/5f), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, 10/16f, 20/16f, 0, SPRITE.getMaxU(), SPRITE.getFrameV(16*(frame-1)/5f), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, 10/16f, 0, 0, SPRITE.getMaxU(), SPRITE.getFrameV(16*frame/5f), LightmapTextureManager.MAX_LIGHT_COORDINATE);

        // second side
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, -10/16f, 20/16f, 0, SPRITE.getMinU(), SPRITE.getFrameV(16*(frame-1)/5f), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, -10/16f, 0, 0, SPRITE.getMinU(), SPRITE.getFrameV(16*frame/5f), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, 10/16f, 0, 0, SPRITE.getMaxU(), SPRITE.getFrameV(16*frame/5f), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        addVertex(poseStack, vc, 1f, 1f, 1f, 1f, 10/16f, 20/16f, 0, SPRITE.getMaxU(), SPRITE.getFrameV(16*(frame-1)/5f), LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }
}
