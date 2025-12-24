package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public class MultipartAnimation implements RenderTheme {
    private final ResourceLocation dataName;
    private final BlockEntityOnly onTick;
    private final BlockEntityOnly onCharge;
    private final BlockEntityOnly onDeplete;
    private final RenderTheme onRender;

    public MultipartAnimation(ResourceLocation dataName, BlockEntityOnly onTick, BlockEntityOnly onCharge, BlockEntityOnly onDeplete, RenderTheme onRender) {
        super();
        this.dataName = dataName;
        this.onTick = onTick;
        this.onCharge = onCharge;
        this.onDeplete = onDeplete;
        this.onRender = onRender;
    }

    @Override
    public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.hasLevel()) return;
        RenderTheme.timedExecution(blockEntity, blockEntity.themeLayout.get(dataName), blockEntity.getLastCharge(), "charging", onCharge);
        RenderTheme.timedExecution(blockEntity, blockEntity.themeLayout.get(dataName), blockEntity.getLastRespawn(), "depletion", onDeplete);
        RenderTheme.tickLoopedExecution(blockEntity, blockEntity.themeLayout.get(dataName), "tick", onTick);
        onRender.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
