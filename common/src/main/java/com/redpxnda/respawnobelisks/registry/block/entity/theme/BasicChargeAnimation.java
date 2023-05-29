package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;

public class BasicChargeAnimation extends NamedRenderTheme {
    private final String name;
    private final RenderTheme handler;

    public BasicChargeAnimation(String name, RenderTheme handler) {
        super(name);
        this.name = name;
        this.handler = handler;
    }

    @Override
    public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.hasLevel()) return;
        long gameTime;
        if (blockEntity.getLevel().getGameTime() == blockEntity.getLastCharge()) System.out.println("render progress: " + blockEntity.renderProgress);
        if ((gameTime = blockEntity.getLevel().getGameTime()) == blockEntity.getLastCharge() && blockEntity.renderProgress < 1f) {
            System.out.println("then rendering");
            handler.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            blockEntity.renderProgress = 1f;
            blockEntity.lastRender = gameTime;
            System.out.println("new: " + blockEntity.renderProgress);
        } else if (gameTime != blockEntity.getLastCharge())
            blockEntity.renderProgress = 0f;
    }

    @Override
    public String getName() {
        return name;
    }
}
