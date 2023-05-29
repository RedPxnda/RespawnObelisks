package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

import static com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBER.*;
import static com.redpxnda.respawnobelisks.util.RenderUtils.renderRunes;

@FunctionalInterface
public interface RenderTheme {
    BasicChargeAnimation defaultCharge = new BasicChargeAnimation("defaultCharge", (be, pt, ps, bs, pl, po) -> {
        System.out.println("attempting to charge render");
        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();
        assert level != null : " Level is somehow null in BasicChargeAnimation";
        level.addParticle(ParticleTypes.EXPLOSION, pos.getX()+0.5, pos.getY()+3, pos.getZ()+0.5, 0, 0, 0);
    });
    BasicDepleteAnimation defaultDeplete = new BasicDepleteAnimation("defaultDeplete", (be, pt, ps, bs, pl, po) -> {
        System.out.println("attempting to deplete render");
        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();
        assert level != null : " Level is somehow null in BasicDepleteAnimation";
        level.addParticle(ParticleTypes.EXPLOSION, pos.getX()+0.5, pos.getY()+3, pos.getZ()+0.5, 0, 0, 0);
    });
    NamedRenderTheme defaultRunes = NamedRenderTheme.of("defaultRunes", (be, pt, ps, bs, pl, po) -> {
        if (SPRITE == null) SPRITE = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(RUNES);
        renderRunes(SPRITE, be, pt, ps, bs, pl);
    });

    void render(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay);
}
