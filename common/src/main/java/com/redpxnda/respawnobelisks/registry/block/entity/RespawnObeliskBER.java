package com.redpxnda.respawnobelisks.registry.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.theme.NamedRenderTheme;
import com.redpxnda.respawnobelisks.registry.block.entity.theme.ObeliskThemeData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;
import static com.redpxnda.respawnobelisks.util.RenderUtils.*;
import static com.redpxnda.nucleus.util.RenderUtil.*;

public class RespawnObeliskBER implements BlockEntityRenderer<RespawnObeliskBlockEntity> {

    public static final ResourceLocation RUNES = new ResourceLocation(MOD_ID, "block/runes");
    public static TextureAtlasSprite SPRITE = null;
    private final BlockEntityRendererProvider.Context context;

    public RespawnObeliskBER(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (blockEntity.themeData == null) blockEntity.themeData = new ObeliskThemeData();
        blockEntity.themes.forEach(str -> {
            NamedRenderTheme theme = NamedRenderTheme.THEMES.get(str);
            if (theme != null)
                theme.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        });
        if (!blockEntity.getObeliskName().equals("") && blockEntity.getObeliskNameComponent() != null)
            renderNameTag(context, blockEntity.hasLimboEntity(), blockEntity.getObeliskNameComponent(), poseStack, bufferSource, packedLight);
        if (blockEntity.hasLimboEntity())
            renderTotemItem(context, blockEntity, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
