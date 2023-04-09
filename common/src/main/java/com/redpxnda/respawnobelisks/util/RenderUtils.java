package com.redpxnda.respawnobelisks.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.particle.packs.IBasicPack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
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
//        registerParticleTexture("circle::0", new ResourceLocation(MOD_ID, "block/circle_rune/background"));
//        registerParticleTexture("circle::1", new ResourceLocation(MOD_ID, "block/circle_rune/circle_rim"));
//        registerParticleTexture("circle::2", new ResourceLocation(MOD_ID, "block/circle_rune/octagon_rim"));
//        registerParticleTexture("circle::3", new ResourceLocation(MOD_ID, "block/circle_rune/middle_inner"));
//        registerParticleTexture("circle::4", new ResourceLocation(MOD_ID, "block/circle_rune/far_inner"));
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

    private static Blaze BLAZE = null;

    public static final Vector3f[] runeCircleColors = {
            new Vector3f(19/255f, 142/255f, 153/255f),
            new Vector3f(41/255f, 223/255f, 235/255f)
    };

    public static void renderBlaze(RespawnObeliskBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher(); // getting rendering manager and disabling shadows
        renderManager.setRenderShadow(false);

        if (Minecraft.getInstance().level == null || be.getLevel() == null) return;
        if (BLAZE == null) BLAZE = new Blaze(EntityType.BLAZE, Minecraft.getInstance().level); // setting blaze if non-existent

        BlockPos pos = be.getBlockPos(); // setting blaze's pos
        BLAZE.setPos(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);

        float renderTicks = be.getLevel().getGameTime() + partialTick;

        poseStack.pushPose(); // rendering blaze
        poseStack.translate(0.375D, -0.65F, 0.6125D);
        poseStack.scale(1.4f, 1.4f, 1.4f);
        BlazeRenderer renderer = (BlazeRenderer) renderManager.getRenderer(BLAZE);
        renderer.getModel().root().getChild("head").visible = false;
        for (int i = 4; i < 12; i++) {
            renderer.getModel().root().getChild("part"+i).visible = false;
        }
        renderManager.render(BLAZE, 0, 0, 0, 0f, renderTicks, poseStack, buffer, 0xFFFFFF);

        renderManager.setRenderShadow(true); // setting things back
        renderer.getModel().root().getChild("head").visible = true;
        for (int i = 4; i < 12; i++) {
            renderer.getModel().root().getChild("part"+i).visible = true;
        }
        poseStack.popPose();
    }

    public static void renderRunes(TextureAtlasSprite sprite, IBasicPack pack, RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
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

    public static void renderRuneCircle(long time, float scale, Vector3f[] colors, float alpha, float x, float y, float z, SpriteSet set, Vector3f[] vertices, VertexConsumer vc, int light) {
        rotateVectors(vertices, Vector3f.XP.rotationDegrees(90));
        TextureAtlasSprite sprite;
        for (int i = 1; i < 5; i++) {
            time*=1 + i/5f;
            if (i % 2 == 0) rotateVectors(vertices, Vector3f.YP.rotationDegrees(time));
            else rotateVectors(vertices, Vector3f.YN.rotationDegrees(time));
            RenderUtils.scaleVectors(vertices, scale);
            sprite = set.get(i, 4);
            RenderUtils.translateVectors(vertices, x, y+(0.01f*i), z);
            addParticleQuad(vertices, vc, colors[i%2].x(), colors[i%2].y(), colors[i%2].z(), alpha, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), light);
            RenderUtils.translateVectors(vertices, -x, -y, -z);
            RenderUtils.scaleVectors(vertices, 1/scale);
            if (i % 2 == 0) rotateVectors(vertices, Vector3f.YN.rotationDegrees(time));
            else rotateVectors(vertices, Vector3f.YP.rotationDegrees(time));
        }
    }

    public static void rotateVectors(Vector3f[] vectors, Quaternion quaternion) {
        for (Vector3f vec : vectors) {
            vec.transform(quaternion);
        }
    }
    public static void translateVectors(Vector3f[] vectors, float x, float y, float z) {
        for (Vector3f vec : vectors) {
            vec.add(x, y, z);
        }
    }
    public static void scaleVectors(Vector3f[] vectors, float amnt) {
        for (Vector3f vec : vectors) {
            vec.mul(amnt);
        }
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

    public static void addParticleQuad(Vector3f[] vertices, VertexConsumer vc, float red, float green, float blue, float alpha, float u0, float u1, float v0, float v1, int light) {
        addParticleVertex(vc, red, green, blue, alpha, vertices[0].x(), vertices[0].y(), vertices[0].z(), u0, v0, light);
        addParticleVertex(vc, red, green, blue, alpha, vertices[1].x(), vertices[1].y(), vertices[1].z(), u0, v1, light);
        addParticleVertex(vc, red, green, blue, alpha, vertices[2].x(), vertices[2].y(), vertices[2].z(), u1, v1, light);
        addParticleVertex(vc, red, green, blue, alpha, vertices[3].x(), vertices[3].y(), vertices[3].z(), u1, v0, light);
    }

    public static void addParticleVertex(VertexConsumer vc, float red, float green, float blue, float alpha, float x, float y, float z, float u, float v, int light) {
        vc.vertex(x, y, z).uv(u, v).color(red, green, blue, alpha).uv2(light).endVertex();
    }

    public static void addVertex(Matrix4f matrix4f, VertexConsumer vc, float red, float green, float blue, float alpha, float x, float y, float z, float u, float v, int light) {
        vc.vertex(matrix4f, x, y, z).color(red, green, blue, alpha).uv(u, v).uv2(light).normal(1, 0, 0).endVertex();
    }
}
