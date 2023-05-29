package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;

public class BasicDepleteAnimation extends NamedRenderTheme {
    private final String name;
    private final RenderTheme handler;

    public BasicDepleteAnimation(String name, RenderTheme handler) {
        super(name);
        this.name = name;
        this.handler = handler;
    }

    @Override
    public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.hasLevel()) return;
        long gameTime;
        if ((gameTime = blockEntity.getLevel().getGameTime()) == blockEntity.getLastRespawn() && blockEntity.renderProgress == 0) {
            handler.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            blockEntity.renderProgress = 1f;
            blockEntity.lastRender = gameTime;
        } else if (blockEntity.getLevel().getGameTime() != blockEntity.getLastRespawn() && blockEntity.renderProgress == 1)
            blockEntity.renderProgress = 0f;
    }

    @Override
    public String getName() {
        return name;
    }
}
