package com.redpxnda.respawnobelisks.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.particle.packs.IBasicPack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

@Environment(EnvType.CLIENT)
public class RenderUtils {
    private static Map<String, ResourceLocation> PACK_TEXTURES = new HashMap<>();
    private static Map<String, TextureAtlasSprite> PACK_SPRITES = new HashMap<>();
    public static Map<String, ResourceLocation> getPackTextures() {
        return PACK_TEXTURES;
    }
    static {
        registerPackTexture("sculk_tendrils", new ResourceLocation("minecraft", "entity/warden/warden"));
        registerPackTexture("rainbow", new ResourceLocation(MOD_ID, "block/rainbow"));
        registerPackTexture("circle::0", new ResourceLocation(MOD_ID, "block/circle_rune/background"));
        registerPackTexture("circle::1", new ResourceLocation(MOD_ID, "block/circle_rune/circle_rim"));
        registerPackTexture("circle::2", new ResourceLocation(MOD_ID, "block/circle_rune/octagon_rim"));
        registerPackTexture("circle::3", new ResourceLocation(MOD_ID, "block/circle_rune/middle_inner"));
        registerPackTexture("circle::4", new ResourceLocation(MOD_ID, "block/circle_rune/far_inner"));
    }
    public static void registerPackTexture(String id, ResourceLocation location) {
        PACK_TEXTURES.put(id, location);
    }
    public static TextureAtlasSprite getAtlasSprite(String sprite) {
        if (!PACK_SPRITES.containsKey(sprite))
            PACK_SPRITES.put(sprite, Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(PACK_TEXTURES.get(sprite)));
        return PACK_SPRITES.get(sprite);
    }

    private static ItemStack TOTEM_STACK = null;

    private static float[][] RUNE_CIRCLE_COLORS = {
            {80/255f, 0/255f, 170/255f},
            {70/255f, 0/255f, 130/255f}
    };

    public static void renderRunes(TextureAtlasSprite sprite, IBasicPack pack, RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.translucent());

        float[] colors = pack.runeColor(partialTick, blockEntity.getLevel());
        poseStack.translate(0.5, 18/16f, 0.5);
        for (int i = 0; i < 4; i++) {
            addQuad(false, poseStack.last().pose(), vc, colors[0], colors[1], colors[2], (float) (blockEntity.getCharge(Minecraft.getInstance().player)/blockEntity.getMaxCharge()), 9/32f, 24/32f, -5.501f/16f, 0, sprite.getU(3), sprite.getU(12), sprite.getV(2), sprite.getV(14), packedLight);
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
        addDoubleQuad(poseStack, vc, 1f, 1f, 1f, 1f, size, size, 0, -11.5f/16f, sprite.getU(58/8f), sprite.getU(68/8f), sprite.getV(6/8f), sprite.getV(16/8f), light);

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
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90*direction));
        addQuad((f, bl) -> (bl ? f : 0f)-size/2, (f, bl) -> (bl ? 0f : f)-size/2, poseStack.last().pose(), vc, 1f, 1f, 1f, alpha, size*progress, size, 0, sprite.getU0(), sprite.getU(progress*16f), sprite.getV0(), sprite.getV1(), light);
        addQuad((f, bl) -> 0f-size/2, (f, bl) -> f-size/2, poseStack.last().pose(), vc, 1f, 1f, 1f, alpha, size*progress, size, 0, sprite.getU0(), sprite.getU(progress*16f), sprite.getV1(), sprite.getV0(), light);

        poseStack.popPose();
    }

    public static void renderRuneCircle(long time, float alpha, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, int light) {
        System.out.println("should be rendering :P " + pos);
        poseStack.pushPose();
        poseStack.translate(pos.getX()+0.5, pos.getY()+1.01, pos.getZ()+0.5);

        VertexConsumer vc = buffer.getBuffer(RenderType.translucent());
        poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
        TextureAtlasSprite sprite = getAtlasSprite("circle::0");
        addQuad(false, poseStack.last().pose(), vc, RUNE_CIRCLE_COLORS[0][0], RUNE_CIRCLE_COLORS[0][1], RUNE_CIRCLE_COLORS[0][2], alpha*0.75f, 2f, 2f, 0, 0, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), light);
        poseStack.translate(0, 0.0, -0.2);
        for (int i = 1; i < 5; i++) {
            time*=1 + i/5f;
            if (i % 2 == 0) poseStack.mulPose(Vector3f.ZP.rotationDegrees(time));
            else poseStack.mulPose(Vector3f.ZN.rotationDegrees(time));
            poseStack.translate(0, 0, 0.01);
            sprite = getAtlasSprite("circle::" + i);
            addQuad(false, poseStack.last().pose(), vc, RUNE_CIRCLE_COLORS[i%2][0], RUNE_CIRCLE_COLORS[i%2][1], RUNE_CIRCLE_COLORS[i%2][2], alpha, 2f, 2f, 0, 0, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), light);
            if (i % 2 == 0) poseStack.mulPose(Vector3f.ZN.rotationDegrees(time));
            else poseStack.mulPose(Vector3f.ZP.rotationDegrees(time));
        }
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
        addQuad(false, stack.last().pose(), vc, red, green, blue, alpha, x, y, z, xOffset, u0, u1, v0, v1, light);
        addQuad(true, stack.last().pose(), vc, red, green, blue, alpha, x, y, z, xOffset, u0, u1, v0, v1, light);
    }

    public static void addQuad(boolean reverse, Matrix4f matrix4f, VertexConsumer vc, float red, float green, float blue, float alpha, float x, float y, float z, float xOffset, float u0, float u1, float v0, float v1, int light) {
        if (reverse)
            addQuad((f, bl) -> bl ? f : -f+xOffset, (f, bl) -> bl ? -f : f+xOffset, matrix4f, vc, red, green, blue, alpha, x, y, z, u1, u0, v0, v1, light);
        else
            addQuad((f, bl) -> bl ? f : f+xOffset, (f, bl) -> bl ? -f : -f+xOffset, matrix4f, vc, red, green, blue, alpha, x, y, z, u0, u1, v0, v1, light);
    }
    public static void addQuad(BiFunction<Float, Boolean, Float> primary, BiFunction<Float, Boolean, Float> secondary, Matrix4f matrix4f, VertexConsumer vc, float red, float green, float blue, float alpha, float x, float y, float z, float u0, float u1, float v0, float v1, int light) {
        addVertex(matrix4f, vc, red, green, blue, alpha, primary.apply(x, false), primary.apply(y, true), z, u0, v0, light);
        addVertex(matrix4f, vc, red, green, blue, alpha, primary.apply(x, false), secondary.apply(y, true), z, u0, v1, light);
        addVertex(matrix4f, vc, red, green, blue, alpha, secondary.apply(x, false), secondary.apply(y, true), z, u1, v1, light);
        addVertex(matrix4f, vc, red, green, blue, alpha, secondary.apply(x, false), primary.apply(y, true), z, u1, v0, light);
    }

    public static void addVertex(Matrix4f matrix4f, VertexConsumer vc, float red, float green, float blue, float alpha, float x, float y, float z, float u, float v, int light) {
        vc.vertex(matrix4f, x, y, z).color(red, green, blue, alpha).uv(u, v).uv2(light).normal(1, 0, 0).endVertex();
    }
}