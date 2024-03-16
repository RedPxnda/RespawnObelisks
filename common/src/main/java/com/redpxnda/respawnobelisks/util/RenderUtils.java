package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.BlazeEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

import java.util.Random;

import static com.redpxnda.nucleus.client.Rendering.*;
import static com.redpxnda.respawnobelisks.registry.ModRegistries.rl;
import static net.minecraft.client.render.RenderPhase.*;

public class RenderUtils {
    public static RenderLayer particleTranslucent = RenderLayer.of(
            "respawn_obelisks_particle_translucent", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS,
            0x200000, true, true,
            RenderLayer.MultiPhaseParameters.builder()
                    .lightmap(ENABLE_LIGHTMAP).program(TRANSLUCENT_PROGRAM)
                    .texture(new Texture(SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE, false, true)).transparency(TRANSLUCENT_TRANSPARENCY)
                    .target(TRANSLUCENT_TARGET).build(true));
    private static BlazeEntity blaze = null;
    private static Sprite sculkSprite;
    private static Sprite sculkTendrilsSprite;

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

    public static void renderBlaze(RespawnObeliskBlockEntity be, float partialTick, MatrixStack poseStack, VertexConsumerProvider buffer) {
        EntityRenderDispatcher renderManager = MinecraftClient.getInstance().getEntityRenderDispatcher(); // getting rendering manager and disabling shadows
        renderManager.setRenderShadows(false);

        if (MinecraftClient.getInstance().world == null || be.getWorld() == null) return;
        if (blaze == null) blaze = new BlazeEntity(EntityType.BLAZE, MinecraftClient.getInstance().world); // setting blaze if non-existent

        BlockPos pos = be.getPos(); // setting blaze's pos
        blaze.setPosition(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);

        float renderTicks = be.getWorld().getTime() + partialTick;

        poseStack.push(); // rendering blaze
        poseStack.translate(0.375D, -0.65F, 0.6125D);
        poseStack.scale(1.4f, 1.4f, 1.4f);
        BlazeEntityRenderer renderer = (BlazeEntityRenderer) renderManager.getRenderer(blaze);
        renderer.getModel().getPart().getChild("head").visible = false;
        for (int i = 4; i < 12; i++) {
            renderer.getModel().getPart().getChild("part"+i).visible = false;
        }
        renderManager.render(blaze, 0, 0, 0, 0f, renderTicks, poseStack, buffer, 0xFFFFFF);

        renderManager.setRenderShadows(true); // setting things back
        renderer.getModel().getPart().getChild("head").visible = true;
        for (int i = 4; i < 12; i++) {
            renderer.getModel().getPart().getChild("part"+i).visible = true;
        }
        poseStack.pop();
    }

    public static void renderRunes(Sprite sprite, RespawnObeliskBlockEntity blockEntity, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        renderRunes(RenderLayer.getTranslucent(), sprite, new float[]{ 1f, 1f, 1f }, blockEntity, partialTick, poseStack, bufferSource, packedLight);
    }

    public static void renderRunes(RenderLayer renderType, Sprite sprite, float[] colors, RespawnObeliskBlockEntity blockEntity, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        poseStack.push();
        VertexConsumer vc = bufferSource.getBuffer(renderType);

        poseStack.translate(0.5, 18/16f, 0.5);
        for (int i = 0; i < 4; i++) {
            addQuad(false, poseStack, vc, colors[0], colors[1], colors[2], (float) (blockEntity.getClientCharge()/blockEntity.getClientMaxCharge()), 9/32f, 24/32f, -5.505f/16f, 0, sprite.getFrameU(3), sprite.getFrameU(12), sprite.getFrameV(2), sprite.getFrameV(14), packedLight);
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        }
        poseStack.pop();
    }

    public static void renderSculkOverlay(RenderLayer renderType, RespawnObeliskBlockEntity blockEntity, double charge, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        if (sculkSprite == null) sculkSprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(rl("block/sculk_animation"));
        poseStack.push();
        VertexConsumer vc = bufferSource.getBuffer(renderType);

        poseStack.translate(0.5, 18/16f, 0.5);
        for (int i = 0; i < 4; i++) {
            addQuad(false, poseStack, vc, 1f, 1f, 1f, (float) (charge/blockEntity.getClientMaxCharge()), 9/32f, 24/32f, -5.505f/16f, 0, sculkSprite.getMinU(), sculkSprite.getFrameU(4.5), sculkSprite.getMinV(), sculkSprite.getFrameV(12), packedLight);
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        }
        poseStack.pop();
    }

    public static void renderSculkTendrils(RespawnObeliskBlockEntity blockEntity, MatrixStack poseStack, VertexConsumerProvider buffer, int light) {
        if (sculkTendrilsSprite == null) sculkTendrilsSprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(rl("block/tendrils"));
        poseStack.push();
        poseStack.translate(0.5, 34/16f, 0.5);

        VertexConsumer vc = buffer.getBuffer(RenderLayer.getTranslucent());
        int direction = 0;
        switch (blockEntity.getCachedState().get(RespawnObeliskBlock.RESPAWN_SIDE)) {
            case EAST -> direction = 1;
            case SOUTH -> direction = 2;
            case WEST -> direction = 3;
        }
        float size = 0.5f*(5/8f);
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90*direction));
        addDoubleQuad(poseStack, vc, 1f, 1f, 1f, 1f, size, size, 0, -11.5f/16f, sculkTendrilsSprite.getMinU(), sculkTendrilsSprite.getFrameU(10), sculkTendrilsSprite.getMinV(), sculkTendrilsSprite.getFrameV(10), light);
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));
        addDoubleQuad(poseStack, vc, 1f, 1f, 1f, 1f, size, size, 0, -11.5f/16f, sculkTendrilsSprite.getMinU(), sculkTendrilsSprite.getFrameU(10), sculkTendrilsSprite.getMinV(), sculkTendrilsSprite.getFrameV(10), light);

        poseStack.pop();
    }

    public static void renderRainbow(float progress, float alpha, RespawnObeliskBlockEntity blockEntity, MatrixStack poseStack, Sprite sprite, VertexConsumerProvider buffer, int light) {
        poseStack.push();
        poseStack.translate(0.5, 40/16f, 0.5);

        VertexConsumer vc = buffer.getBuffer(RenderLayer.getTranslucent());
        int direction = 0;
        switch (blockEntity.getCachedState().get(RespawnObeliskBlock.RESPAWN_SIDE)) {
            case EAST -> direction = 1;
            case SOUTH -> direction = 2;
            case WEST -> direction = 3;
        }
        float size = 4;
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90*direction));
        addQuad((f, bl) -> (bl ? f : 0f)-size/2, (f, bl) -> (bl ? 0f : f)-size/2, poseStack, vc, 1f, 1f, 1f, alpha, size*progress, size, 0, sprite.getMinU(), sprite.getFrameU(progress*16f), sprite.getMinV(), sprite.getMaxV(), light);
        addQuad((f, bl) -> 0f-size/2, (f, bl) -> f-size/2, poseStack, vc, 1f, 1f, 1f, alpha, size*progress, size, 0, sprite.getMinU(), sprite.getFrameU(progress*16f), sprite.getMaxV(), sprite.getMinV(), light);

        poseStack.pop();
    }

    public static void renderRuneCircle(long time, float scale, Vector3f[] colors, float alpha, float x, float y, float z, SpriteProvider set, Vector3f[] vertices, VertexConsumer vc, int light) {
        rotateVectors(vertices, RotationAxis.POSITIVE_X.rotationDegrees(90));
        Sprite sprite;
        for (int i = 1; i < 5; i++) {
            time*=1 + i/5f;
            if (i % 2 == 0) rotateVectors(vertices, RotationAxis.POSITIVE_Y.rotationDegrees(time));
            else rotateVectors(vertices, RotationAxis.NEGATIVE_Y.rotationDegrees(time));
            scaleVectors(vertices, scale);
            sprite = set.getSprite(i, 4);
            translateVectors(vertices, x, y+(0.01f*i), z);
            addParticleQuad(vertices, vc, colors[i%2].x(), colors[i%2].y(), colors[i%2].z(), alpha, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), light);
            translateVectors(vertices, -x, -y, -z);
            scaleVectors(vertices, 1/scale);
            if (i % 2 == 0) rotateVectors(vertices, RotationAxis.NEGATIVE_Y.rotationDegrees(time));
            else rotateVectors(vertices, RotationAxis.POSITIVE_Y.rotationDegrees(time));
        }
    }

    public static void renderTotemItem(BlockEntityRendererFactory.Context context, RespawnObeliskBlockEntity blockEntity, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        poseStack.push();
        poseStack.translate(0.5, 2.5, 0.5);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        if (blockEntity.getWorld() != null) poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(blockEntity.getGameTime() % 360));
        ItemRenderer itemRenderer = context.getItemRenderer();
        itemRenderer.renderItem(
                RespawnObelisksConfig.INSTANCE.revival.revivalItem.getDefaultStack(),
                ModelTransformationMode.GROUND,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                blockEntity.getWorld(),
                1
        );
        poseStack.pop();
    }
}
