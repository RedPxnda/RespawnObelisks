package com.redpxnda.respawnobelisks.registry.block.entity;

import com.redpxnda.respawnobelisks.registry.block.entity.theme.ThemeLayout;
import com.redpxnda.respawnobelisks.registry.block.entity.theme.RenderTheme;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;
import static com.redpxnda.respawnobelisks.util.RenderUtils.*;
import static com.redpxnda.nucleus.client.Rendering.*;

public class RespawnObeliskBER implements BlockEntityRenderer<RespawnObeliskBlockEntity> {

    public static final Identifier RUNES = new Identifier(MOD_ID, "block/runes");
    public static Sprite SPRITE = null;
    private final BlockEntityRendererFactory.Context context;

    public RespawnObeliskBER(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        if (blockEntity.themeLayout == null) blockEntity.themeLayout = new ThemeLayout();
        blockEntity.getThemes().forEach(rl -> {
            RenderTheme theme = RenderTheme.themes.get(rl);
            if (theme != null)
                theme.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        });
        if (!blockEntity.getObeliskName().equals("") && blockEntity.getObeliskNameComponent() != null)
            renderNameTag(context, blockEntity.hasLimboEntity(), blockEntity.getObeliskNameComponent(), poseStack, bufferSource, packedLight);
        if (blockEntity.hasLimboEntity())
            renderTotemItem(context, blockEntity, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
