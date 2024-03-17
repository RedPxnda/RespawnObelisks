package com.redpxnda.respawnobelisks.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redpxnda.nucleus.math.MathUtil;
import com.redpxnda.nucleus.util.Color;
import com.redpxnda.nucleus.util.MiscUtil;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import com.redpxnda.respawnobelisks.network.FinishPriorityChangePacket;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.ScrollWheelPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBER;
import com.redpxnda.respawnobelisks.registry.particle.ChargeIndicatorParticle;
import com.redpxnda.respawnobelisks.registry.particle.DepleteRingParticle;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;

public class ClientEvents {
    protected static void onHudRender(DrawContext graphics, float tickDelta) {
        if (!RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.enableSecondarySpawnPoints || !RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.allowPriorityShifting) return;
        float alpha = -1;
        SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(MinecraftClient.getInstance().player);
        if (facet == null || facet.reorderingTarget == null) return;
        if (!ClientUtils.hasLookedAwayFromPriorityChanger && MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult blockHitResult && blockHitResult.getBlockPos().equals(facet.reorderingTarget.pos())) {
            alpha = 1;
            ClientUtils.priorityChangerLookAwayTime = Util.getMeasuringTimeMs();
        } else if (ClientUtils.priorityChangerLookAwayTime >= 0) {
            float delta = 1 - MathHelper.clamp((Util.getMeasuringTimeMs()-ClientUtils.priorityChangerLookAwayTime)/1000f, 0, 1);
            delta = MathUtil.flip(MathUtil.pow(MathUtil.flip(delta), 3));
            if (delta == 0) {
                ClientUtils.priorityChangerLookAwayTime = -100;
            } else alpha = delta;
            ClientUtils.hasLookedAwayFromPriorityChanger = true;
        }

        if (alpha != -1) {
            int targetIndex = facet.points.indexOf(facet.reorderingTarget);
            if (targetIndex == -1) return;
            int size = facet.points.size();

            int minIndex;
            int maxIndex;

            if (size <= 5) {
                minIndex = 0;
                maxIndex = size;
            } else if ((float) targetIndex/size <= 0.5) {
                minIndex = Math.max(0, targetIndex-2);
                maxIndex = Math.min(minIndex+5, facet.points.size());
            } else {
                maxIndex = Math.min(targetIndex+3, facet.points.size());
                minIndex = Math.max(0, maxIndex-5);
            }

            int x = graphics.getScaledWindowWidth()/2 + 20;
            int y = graphics.getScaledWindowHeight()/2 - Math.min(maxIndex, 5)*12;
            float invAlpha = 1-alpha;
            graphics.enableScissor(x-9, (int) (y - 2 + invAlpha*maxIndex*12), graphics.getScaledWindowWidth(), (int) (y + 2 + maxIndex*24 - invAlpha*maxIndex*12));
            y+=4;
            RenderSystem.enableBlend();
            for (int i = minIndex; i < maxIndex; i++) {
                SpawnPoint point = facet.points.get(i);
                Item item = ClientUtils.cachedSpawnPointItems.getOrDefault(point, Items.AIR);

                graphics.drawItem(item.getDefaultStack(), x, y);
                Text text = Text.translatable(item.getTranslationKey()).append(Text.literal(" @(" + point.pos().getX() + ", " + point.pos().getY() + ", " + point.pos().getZ() + ")"));
                Text dimensionText = Text.literal(point.dimension().getValue().toString());
                graphics.drawText(MinecraftClient.getInstance().textRenderer, text, x+20, y-1, Color.WHITE.argb(), true);
                graphics.drawText(MinecraftClient.getInstance().textRenderer, dimensionText, x+20, y+9, Color.TEXT_GRAY.argb(), true);
                if (point.equals(facet.reorderingTarget)) {
                    graphics.fill(x-2, y-2, x+18, y-1, Color.WHITE.argb());
                    graphics.fill(x+17, y-2, x+18, y+18, Color.WHITE.argb());
                    graphics.fill(x-2, y+17, x+18, y+18, Color.WHITE.argb());
                    graphics.fill(x-2, y-2, x-1, y+18, Color.WHITE.argb());
                }

                if (i == minIndex)
                    graphics.drawText(MinecraftClient.getInstance().textRenderer, "-", x-8, y+4, Color.WHITE.argb(), false);
                else if (i == maxIndex-1)
                    graphics.drawText(MinecraftClient.getInstance().textRenderer, "+", x-8, y+4, Color.WHITE.argb(), false);

                y+=24;
            }
            RenderSystem.disableBlend();
            graphics.disableScissor();
        }
    }

    protected static EventResult onClientScroll(MinecraftClient mc, double amount) {
        ClientPlayerEntity player = mc.player;
        if (player == null || !player.isSneaking()) return EventResult.pass();
        if (mc.crosshairTarget instanceof BlockHitResult blockResult && mc.world != null) {
            SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(MinecraftClient.getInstance().player);
            if (RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.allowPriorityShifting && !ClientUtils.hasLookedAwayFromPriorityChanger && facet != null && facet.reorderingTarget != null) {
                if (amount > 0) MiscUtil.moveListElementUp(facet.points, facet.reorderingTarget);
                else MiscUtil.moveListElementDown(facet.points, facet.reorderingTarget);
                ModPackets.CHANNEL.sendToServer(new FinishPriorityChangePacket(facet.points));
                return EventResult.interruptFalse();
            }
            BlockState blockState = mc.world.getBlockState(blockResult.getBlockPos());
            if (!(blockState.getBlock() instanceof RespawnObeliskBlock)) return EventResult.pass();
            boolean isUpper = !(blockState.get(RespawnObeliskBlock.HALF) == DoubleBlockHalf.LOWER);
            mc.world.playSound(player, blockResult.getBlockPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 1, 1);
            ModPackets.CHANNEL.sendToServer(new ScrollWheelPacket(amount, blockResult, isUpper));
            return EventResult.interruptFalse();
        }

        return EventResult.pass();
    }

    public static void onClientSetup(MinecraftClient mc) {
        BlockEntityRendererRegistry.register(ModRegistries.ROBE.get(), RespawnObeliskBER::new);
//        if (Platform.isFabric()) // idk this is super goofy: libraries say the `register` method is public, I even AW'd it, but still I get errors :P
//            ItemPropertiesAccessor.register(ModRegistries.BOUND_COMPASS.get(), new ResourceLocation("angle"), new CompassItemPropertyFunction((level, stack, player) -> BoundCompassItem.isLodestoneCompass(stack) ? BoundCompassItem.getLodestonePosition(stack.getOrCreateTag()) : null));
    }

    public static void init() {
        ClientRawInputEvent.MOUSE_SCROLLED.register(ClientEvents::onClientScroll);
        ClientLifecycleEvent.CLIENT_SETUP.register(ClientEvents::onClientSetup);
        ClientGuiEvent.RENDER_HUD.register(ClientEvents::onHudRender);
    }

    public static void registerParticleProviders() {
        ParticleProviderRegistry.register(ModRegistries.depleteRingParticle, DepleteRingParticle.Provider::new);
        ParticleProviderRegistry.register(ModRegistries.chargeIndicatorParticle, ChargeIndicatorParticle.Provider::new);
    }
}
