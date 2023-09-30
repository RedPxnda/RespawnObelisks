package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.nucleus.math.ParticleShaper;
import com.redpxnda.nucleus.client.Rendering;
import com.redpxnda.respawnobelisks.config.ChargeConfig;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.redpxnda.nucleus.client.Rendering.lerpColors;
import static com.redpxnda.respawnobelisks.registry.ModRegistries.rl;
import static com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBER.*;
import static com.redpxnda.respawnobelisks.registry.block.entity.theme.ThemeLayout.ThemeData;
import static com.redpxnda.respawnobelisks.util.RenderUtils.*;

@FunctionalInterface
public interface RenderTheme {
    Map<ResourceLocation, RenderTheme> themes = new HashMap<>();
    Random rdm = new Random();

    ResourceLocation defCharge = rl("default_charge");
    ResourceLocation defDep = rl("default_deplete");
    ResourceLocation defRunes = rl("default_runes");
    ResourceLocation sculk = rl("sculk");
    ResourceLocation blazing = rl("blazing");

    static void init() {
        register(defCharge, (be, pt, ps, bs, pl, po) -> {
            Level level = be.getLevel();
            if (level == null) return;
            Minecraft mc = Minecraft.getInstance();
            ThemeData data = be.themeLayout.get(defCharge);
            BlockPos pos = be.getBlockPos();
            if (mc.player != null) {
                if (
                        rdm.nextDouble() > 0.95 &&
                        mc.hitResult instanceof BlockHitResult bhr &&
                        (be.getBlockPos().equals(bhr.getBlockPos()) || be.getBlockPos().equals(bhr.getBlockPos().below())) &&
                        ChargeConfig.getChargeItems().containsKey(mc.player.getMainHandItem().getItem())
                ) {
                    tickLoopedExecution(be, data, "defaultCharge", blockEntity -> {
                        if (be.getLastCharge() < level.getGameTime() - 50) {
                            double rX = rdm.nextDouble(6) - 3;
                            double rY = rdm.nextDouble(1.75);
                            double rZ = rdm.nextDouble(6) - 3;
                            level.addParticle(ModRegistries.chargeIndicatorParticle.get(), pos.getX() + 0.5 + rX, pos.getY() + rY, pos.getZ() + 0.5 + rZ, pos.getX()+0.5, pos.getY() + rY, pos.getZ()+0.5);
                        }
                    });
                }
            }
            timedExecution(be, data, be.getLastCharge(), "defaultCharge-main", x -> {
                level.addParticle(ParticleTypes.FLASH, pos.getX() + 0.5, pos.getY()+0.1, pos.getZ() + 0.5, 0, 0, 0);
                level.playLocalSound(
                        pos.getX(), pos.getY(), pos.getZ(),
                        BuiltInRegistries.SOUND_EVENT.getOptional(new ResourceLocation(ChargeConfig.obeliskChargeSound)).orElse(SoundEvents.UI_BUTTON_CLICK.value()), SoundSource.BLOCKS,
                        1, 1, false
                );
            });
        });
        register(defDep, new BasicDepleteAnimation(defDep, "time", be -> {
            Level level = be.getLevel();
            BlockPos pos = be.getBlockPos();
            assert level != null : " Level is somehow null in BasicDepleteAnimation";
            level.playLocalSound(
                    pos.getX(), pos.getY(), pos.getZ(),
                    BuiltInRegistries.SOUND_EVENT.getOptional(new ResourceLocation(ChargeConfig.obeliskDepleteSound)).orElse(SoundEvents.UI_BUTTON_CLICK.value()), SoundSource.BLOCKS,
                    1, 1, false
            );
            level.addParticle(ModRegistries.depleteRingParticle.get(), pos.getX()+0.5, pos.getY()+1.05, pos.getZ()+0.5, 0, 0, 0);
        }));
        register(defRunes, (be, pt, ps, bs, pl, po) -> {
            if (SPRITE == null) SPRITE = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(RUNES);
            renderRunes(SPRITE, be, pt, ps, bs, pl);
        });
        register(sculk, (be, pt, ps, bs, pl, po) -> {
            Level level = be.getLevel();
            if (level == null) return;
            BlockPos pos = be.getBlockPos();
            ThemeData data = be.themeLayout.get(sculk);

            tickLoopedExecution(be, data, "tick", x -> {
                double charge = data.getDouble("lastCharge", be.getClientCharge());
                if (charge != be.clientCharge) {
                    if (charge > be.clientCharge) {
                        if (rdm.nextBoolean())
                            level.playLocalSound(
                                    pos.getX(), pos.getY(), pos.getZ(),
                                    SoundEvents.SCULK_BLOCK_BREAK, SoundSource.BLOCKS,
                                    0.5f, 1, false
                            );
                        charge = Math.max(charge - 0.5, be.clientCharge);
                    } else {
                        if (be.getLastCharge()+10 <= be.getGameTime() && rdm.nextBoolean()) {
                            level.playLocalSound(
                                    pos.getX(), pos.getY(), pos.getZ(),
                                    SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS,
                                    1, 1, false
                            );
                            level.addParticle(
                                    ParticleTypes.SCULK_CHARGE_POP,
                                    pos.getX() + rdm.nextDouble(2) - 1,
                                    pos.getY() + rdm.nextDouble(2),
                                    pos.getZ() + rdm.nextDouble(2) - 1,
                                    0, 0, 0
                            );
                        }
                        charge = Math.min(charge + 0.5, be.clientCharge);
                    }
                    data.put("lastCharge", charge);
                }
            });
            timedExecution(be, data, be.getLastCharge(), "charging", x -> level.playLocalSound(
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS,
                    25, 1, false
            ));
            renderSculkTendrils(be, ps, bs, pl);
            renderSculkOverlay(Rendering.alphaAnimation, be, data.getDouble("lastCharge", be.getClientCharge()), pt, ps, bs, pl);
        });
        register(blazing, new MultipartAnimation(blazing, be -> {
            Level level = be.getLevel();
            assert level != null;
            BlockPos pos = be.getBlockPos();

            if (rdm.nextDouble() > 0.95) {
                int count = randomInt(rdm, 3, 6);
                double yStart = randomDouble(rdm, 0.1, 0.2);
                double yInc = randomDouble(rdm, 0.7, 0.75);
                ParticleShaper.square(ParticleTypes.FLAME, 2, count, 1).fromClient().runAt(level, pos.getX()+0.5, pos.getY()+1.5, pos.getZ()+0.5);
            }
        }, be -> {
            Level level = be.getLevel();
            assert level != null;
            BlockPos pos = be.getBlockPos();

            ParticleShaper.square(ParticleTypes.FLAME, 3, 100, 1).fromClient().runAt(level, pos.getX()+0.5, pos.getY()+1.5, pos.getZ()+0.5);
            level.playLocalSound(
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.BLAZE_HURT, SoundSource.BLOCKS,
                    1, 1, false
            );
        }, be -> {
            Level level = be.getLevel();
            assert level != null;
            BlockPos pos = be.getBlockPos();

            //ParticleShaper.expandingSquare(ParticleTypes.FLAME, 3, 100, 1, -0.125).fromClient().runAt(level, pos.getX()+1.5, pos.getY()+1.5, pos.getZ()+0.5);
            level.playLocalSound(
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.BLAZE_DEATH, SoundSource.BLOCKS,
                    1, 1, false
            );
        }, (be, pt, ps, bs, pl, po) -> {
            Level level = be.getLevel();
            assert level != null;

            renderBlaze(be, pt, ps, bs);
            if (SPRITE == null) SPRITE = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(RUNES);
            renderRunes(RenderType.translucent(), SPRITE, lerpColors(level.getGameTime(), 100, new float[][] {
                    { 255, 50, 0 },
                    { 255, 175, 0 }
            }), be, pt, ps, bs, pl);
        }));
    }

    static void register(ResourceLocation name, RenderTheme theme) {
        themes.put(name, theme);
    }

    static void timedExecution(RespawnObeliskBlockEntity blockEntity, ThemeData data, long checkAgainst, String name, BlockEntityOnly handler, BlockEntityOnly ifFailed) {
        long gameTime = blockEntity.getGameTime();
        float renderProgress = data.getFloat(name, 0f);
        if (gameTime-3 <= checkAgainst && renderProgress < 1f) {
            handler.call(blockEntity);
            data.put(name, 1f);
        } else if (gameTime-3 > checkAgainst && renderProgress >= 1f) {
            data.put(name, 0f);
            ifFailed.call(blockEntity);
        }
    }
    static void timedExecution(RespawnObeliskBlockEntity blockEntity, ThemeData data, long checkAgainst, String name, BlockEntityOnly handler) {
        timedExecution(blockEntity, data, checkAgainst, name, handler, be -> {});
    }

    static void tickLoopedExecution(RespawnObeliskBlockEntity blockEntity, ThemeData data, String name, BlockEntityOnly handler) {
        long gameTime = blockEntity.getGameTime();
        long checkAgainst = data.getLong(name, 0L);
        if (gameTime != checkAgainst) {
            handler.call(blockEntity);
            data.put(name, gameTime);
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
