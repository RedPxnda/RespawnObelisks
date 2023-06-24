package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public class BasicChargeAnimation implements RenderTheme {
    private final String storageName;
    private final ResourceLocation dataName;
    private final RenderTheme.BlockEntityOnly handler;

    public BasicChargeAnimation(ResourceLocation dataName, String name, RenderTheme.BlockEntityOnly handler) {
        super();
        this.dataName = dataName;
        this.storageName = name;
        this.handler = handler;
    }

    @Override
    public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        RenderTheme.timedExecution(blockEntity, blockEntity.themeLayout.get(dataName), blockEntity.getLastCharge(), storageName, handler);
    }
}
