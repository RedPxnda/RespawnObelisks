package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BasicChargeAnimation implements RenderTheme {
    private final String storageName;
    private final Identifier dataName;
    private final BlockEntityOnly handler;

    public BasicChargeAnimation(Identifier dataName, String name, BlockEntityOnly handler) {
        super();
        this.dataName = dataName;
        this.storageName = name;
        this.handler = handler;
    }

    @Override
    public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        RenderTheme.timedExecution(blockEntity, blockEntity.themeLayout.get(dataName), blockEntity.getLastCharge(), storageName, handler);
    }
}
