package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class MultipartAnimation implements RenderTheme {
    private final Identifier dataName;
    private final BlockEntityOnly onTick;
    private final BlockEntityOnly onCharge;
    private final BlockEntityOnly onDeplete;
    private final RenderTheme onRender;

    public MultipartAnimation(Identifier dataName, BlockEntityOnly onTick, BlockEntityOnly onCharge, BlockEntityOnly onDeplete, RenderTheme onRender) {
        super();
        this.dataName = dataName;
        this.onTick = onTick;
        this.onCharge = onCharge;
        this.onDeplete = onDeplete;
        this.onRender = onRender;
    }

    @Override
    public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.hasWorld()) return;
        RenderTheme.timedExecution(blockEntity, blockEntity.themeLayout.get(dataName), blockEntity.getLastCharge(), "charging", onCharge);
        RenderTheme.timedExecution(blockEntity, blockEntity.themeLayout.get(dataName), blockEntity.getLastRespawn(), "depletion", onDeplete);
        RenderTheme.tickLoopedExecution(blockEntity, blockEntity.themeLayout.get(dataName), "tick", onTick);
        onRender.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
