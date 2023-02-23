package com.redpxnda.respawnobelisks.event;

import com.redpxnda.respawnobelisks.network.Packets;
import com.redpxnda.respawnobelisks.network.ScrollWheelPacket;
import com.redpxnda.respawnobelisks.registry.Registry;
import com.redpxnda.respawnobelisks.registry.blocks.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.blocks.render.RespawnObeliskBER;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MODID;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onClientScroll(InputEvent.MouseScrollingEvent event) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null && !player.isShiftKeyDown()) return;
            if (mc.hitResult instanceof BlockHitResult blockResult && mc.level != null) {
                BlockState blockState = mc.level.getBlockState(blockResult.getBlockPos());
                if (!(blockState.getBlock() instanceof RespawnObeliskBlock)) return;
                else if (!(blockState.getValue(RespawnObeliskBlock.HALF) == DoubleBlockHalf.LOWER)) return;
                mc.level.playSound(player, blockResult.getBlockPos(), SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 1, 1);
                Packets.sendToServer(new ScrollWheelPacket(event.getScrollDelta(), blockResult));
                event.setCanceled(true);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(Registry.RESPAWN_OBELISK_BE.get(), RespawnObeliskBER::new);
        }

        @SubscribeEvent
        public static void onTextureStitch(TextureStitchEvent.Pre event) {
            if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) return;
            event.addSprite(RespawnObeliskBER.RUNES);
        }
    }
}
