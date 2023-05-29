package com.redpxnda.respawnobelisks.registry.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.theme.NamedRenderTheme;
import com.redpxnda.respawnobelisks.registry.block.entity.theme.RenderTheme;
import com.redpxnda.respawnobelisks.registry.particle.packs.IBasicPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;
import static com.redpxnda.respawnobelisks.util.RenderUtils.*;

public class RespawnObeliskBER implements BlockEntityRenderer<RespawnObeliskBlockEntity> {

    public static final ResourceLocation RUNES = new ResourceLocation(MOD_ID, "block/runes");
    public static TextureAtlasSprite SPRITE = null;
    private final BlockEntityRendererProvider.Context context;

    static {
        var if_i_am_initialized_then_so_are_you = RenderTheme.defaultCharge;
    }

    public RespawnObeliskBER(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        blockEntity.themes.forEach(str -> {
            if (NamedRenderTheme.THEMES.containsKey(str))
                NamedRenderTheme.THEMES.get(str).render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        });
        if (!blockEntity.getObeliskName().equals("") && blockEntity.getObeliskNameComponent() != null)
            renderNameTag(context, blockEntity.hasLimboEntity(), blockEntity.getObeliskNameComponent(), poseStack, bufferSource, packedLight);
        if (blockEntity.hasLimboEntity())
            renderTotemItem(context, blockEntity, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
