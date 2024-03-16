package com.redpxnda.respawnobelisks.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.redpxnda.nucleus.math.MathUtil;
import com.redpxnda.nucleus.util.Color;
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
        float alpha = -1;
        if (ClientUtils.focusedPriorityChanger != null && MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult blockHitResult && blockHitResult.getBlockPos().equals(ClientUtils.focusedPriorityChanger.pos())) {
            alpha = 1;
            ClientUtils.priorityChangerLookAwayTime = Util.getMeasuringTimeMs();
        } else if (ClientUtils.priorityChangerLookAwayTime >= 0) {
            float delta = 1 - MathHelper.clamp((Util.getMeasuringTimeMs()-ClientUtils.priorityChangerLookAwayTime)/1000f, 0, 1);
            delta = MathUtil.flip(MathUtil.pow(MathUtil.flip(delta), 3));
            if (delta == 0) {
                ClientUtils.priorityChangerLookAwayTime = -100;
            } else alpha = delta;
            ClientUtils.focusedPriorityChanger = null;
        }

        if (alpha != -1) {
            int maxIndex = Math.min(5, ClientUtils.allCachedSpawnPoints.size());
            int x = graphics.getScaledWindowWidth()/2 + 20;
            int y = graphics.getScaledWindowHeight()/2 - maxIndex*10;
            graphics.enableScissor(x, (int) ((y-4)+((1-alpha)*maxIndex*10)), graphics.getScaledWindowWidth(), (int) (y+maxIndex*20 - ((1-alpha)*maxIndex*10)));
            for (int i = 0; i < maxIndex; i++) {
                SpawnPoint point = ClientUtils.allCachedSpawnPoints.get(i);
                Item item = ClientUtils.cachedSpawnPointItems.getOrDefault(point, Items.AIR);

                RenderSystem.enableBlend();
                graphics.drawItem(item.getDefaultStack(), x, y);
                Text text = Text.translatable(item.getTranslationKey()).append(Text.literal(" @(" + point.pos().getX() + ", " + point.pos().getY() + ", " + point.pos().getZ() + ")"));
                RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
                graphics.drawText(MinecraftClient.getInstance().textRenderer, text, x+20, y+4, Color.WHITE.argb(), true);
                if (i == ClientUtils.priorityChangerIndex) graphics.drawHorizontalLine(x, x+16, y-2, Color.WHITE.argb());
                y+=24;
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
            }
            graphics.disableScissor();
        }
    }

    protected static EventResult onClientScroll(MinecraftClient mc, double amount) {
        ClientPlayerEntity player = mc.player;
        if (player == null || !player.isSneaking()) return EventResult.pass();
        if (mc.crosshairTarget instanceof BlockHitResult blockResult && mc.world != null) {
            if (ClientUtils.focusedPriorityChanger != null) {
                ClientUtils.priorityChangerIndex = MathHelper.clamp(ClientUtils.priorityChangerIndex + (amount > 0 ? 1 : -1), 0, ClientUtils.allCachedSpawnPoints.size()-1);
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
