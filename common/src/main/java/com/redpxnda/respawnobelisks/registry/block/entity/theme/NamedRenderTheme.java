package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.HashMap;
import java.util.Map;

public abstract class NamedRenderTheme implements RenderTheme {
    public static Map<String, NamedRenderTheme> THEMES = new HashMap<>();

    NamedRenderTheme() {
    }

    public static NamedRenderTheme of(String name, RenderTheme handler) {
        return new NamedRenderTheme() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public void render(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
                handler.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            }
        };
    }

    public abstract String getName();
}
