package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;

public class BasicChargeAnimation extends NamedRenderTheme {
    private final String name;
    private final RenderTheme.BlockEntityOnly handler;

    public BasicChargeAnimation(String name, RenderTheme.BlockEntityOnly handler) {
        super();
        this.name = name;
        this.handler = handler;
    }

    @Override
    public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        RenderTheme.timedExecution(blockEntity, blockEntity.getLastCharge(), name, handler);
    }

    @Override
    public String getName() {
        return name;
    }
}
