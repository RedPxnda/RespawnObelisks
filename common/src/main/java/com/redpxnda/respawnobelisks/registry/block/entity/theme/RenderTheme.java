package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.redpxnda.respawnobelisks.config.ChargeConfig;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

import static com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBER.*;
import static com.redpxnda.respawnobelisks.util.RenderUtils.renderRunes;

@FunctionalInterface
public interface RenderTheme {
    Random rdm = new Random();

    static void init() {
        register(NamedRenderTheme.of("defaultCharge", (be, pt, ps, bs, pl, po) -> {
            Level level = be.getLevel();
            Minecraft mc;
            if (level == null || (mc = Minecraft.getInstance()) == null) return;
            BlockPos pos = be.getBlockPos();
            if (mc.player != null) {
                if (
                        rdm.nextDouble() > 0.8 &&
                        mc.hitResult instanceof BlockHitResult bhr &&
                        (be.getBlockPos().equals(bhr.getBlockPos()) || be.getBlockPos().equals(bhr.getBlockPos().below())) &&
                        ChargeConfig.getChargeItems().containsKey(mc.player.getMainHandItem().getItem())
                ) {
                    tickLoopedExecution(be, "defaultCharge", blockEntity -> {
                        if (be.getLastCharge() < level.getGameTime() - 50) {
                            double rX = rdm.nextDouble(6) - 3;
                            double rZ = rdm.nextDouble(6) - 3;
                            level.addParticle(ModRegistries.CHARGE_INDICATOR_PARTICLE.get(), pos.getX() + 0.5 + rX, pos.getY() + rdm.nextDouble(1.75), pos.getZ() + 0.5 + rZ, pos.getX(), pos.getY(), pos.getZ());
                        }
                    });
                }
                ps.pushPose();
                VertexConsumer vc = bs.getBuffer(RenderType.lines());

                RenderUtils.CHARGE_PARTICLES.get(pos).forEach(particle -> {
                    vc.vertex(ps.last().pose(), 0.5f, (float) (particle.getY() - pos.getY()), 0.5f).color(1f, 1f, 1f, particle.alpha).normal(1, 0, 0).endVertex();
                    vc.vertex(ps.last().pose(), (float) (particle.getX() - pos.getX()), (float) (particle.getY() - pos.getY()), (float) (particle.getZ() - pos.getZ())).color(1f, 1f, 1f, particle.alpha).normal(1, 0, 0).endVertex();
                });

                ps.popPose();
            }
            timedExecution(be, be.getLastCharge(), "defaultCharge-main", x -> {
                level.addParticle(ParticleTypes.FLASH, pos.getX() + 0.5, pos.getY()+0.1, pos.getZ() + 0.5, 0, 0, 0);
                level.playLocalSound(
                        pos.getX(), pos.getY(), pos.getZ(),
                        Registry.SOUND_EVENT.getOptional(new ResourceLocation(ChargeConfig.obeliskChargeSound)).orElse(SoundEvents.UI_BUTTON_CLICK), SoundSource.BLOCKS,
                        1, 1, false
                );
            });
        }));
        register(new BasicDepleteAnimation("defaultDeplete", be -> {
            Level level = be.getLevel();
            BlockPos pos = be.getBlockPos();
            assert level != null : " Level is somehow null in BasicDepleteAnimation";
            level.playLocalSound(
                    pos.getX(), pos.getY(), pos.getZ(),
                    Registry.SOUND_EVENT.getOptional(new ResourceLocation(ChargeConfig.obeliskDepleteSound)).orElse(SoundEvents.UI_BUTTON_CLICK), SoundSource.BLOCKS,
                    1, 1, false
            );
            level.addParticle(ModRegistries.DEPLETE_RING_PARTICLE.get(), pos.getX()+0.5, pos.getY()+1.05, pos.getZ()+0.5, 0, 0, 0);
        }));
        register(NamedRenderTheme.of("defaultRunes", (be, pt, ps, bs, pl, po) -> {
            if (SPRITE == null) SPRITE = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(RUNES);
            renderRunes(SPRITE, be, pt, ps, bs, pl);
        }));
    }

    static void register(NamedRenderTheme theme) {
        NamedRenderTheme.THEMES.put(theme.getName(), theme);
    }
    static void register(NamedRenderTheme theme, String name) {
        NamedRenderTheme.THEMES.put(name, theme);
    }

    static void timedExecution(RespawnObeliskBlockEntity blockEntity, long checkAgainst, String name, BlockEntityOnly handler, BlockEntityOnly ifFailed) {
        long gameTime = blockEntity.getGameTime();
        float renderProgress = blockEntity.themeData.get(name);
        if (gameTime-3 <= checkAgainst && renderProgress < 1f) {
            handler.call(blockEntity);
            blockEntity.themeData.put(name, 1f);
        } else if (gameTime-3 > checkAgainst && renderProgress >= 1f) {
            blockEntity.themeData.put(name, 0f);
            ifFailed.call(blockEntity);
        }
    }
    static void timedExecution(RespawnObeliskBlockEntity blockEntity, long checkAgainst, String name, BlockEntityOnly handler) {
        timedExecution(blockEntity, checkAgainst, name, handler, be -> {});
    }

    static void tickLoopedExecution(RespawnObeliskBlockEntity blockEntity, String name, BlockEntityOnly handler) {
        long gameTime = blockEntity.getGameTime();
        long checkAgainst = blockEntity.themeData.getLong(name);
        if (gameTime != checkAgainst) {
            handler.call(blockEntity);
            blockEntity.themeData.putLong(name, gameTime);
        }
    }

    void render(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay);

    @FunctionalInterface
    interface BlockEntityOnly extends RenderTheme {
        void call(RespawnObeliskBlockEntity blockEntity);

        default void render(RespawnObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
            call(blockEntity);
        }
    }
}
