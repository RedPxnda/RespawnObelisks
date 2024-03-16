package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BasicDepleteAnimation implements RenderTheme {
    private final String storageName;
    private final Identifier dataName;
    private final BlockEntityOnly handler;

    public BasicDepleteAnimation(Identifier dataName, String name, BlockEntityOnly handler) {
        super();
        this.dataName = dataName;
        this.storageName = name;
        this.handler = handler;
    }

    @Override
    public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        RenderTheme.timedExecution(blockEntity, blockEntity.themeLayout.get(dataName), blockEntity.getLastRespawn(), storageName, handler);
    }
}
