package com.redpxnda.respawnobelisks.event;

import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.ScrollWheelPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBER;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTextureStitchEvent;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ClientEvents {
    protected static EventResult onClientScroll(Minecraft mc, double amount) {
        LocalPlayer player = mc.player;
        if (player == null || !player.isShiftKeyDown()) return EventResult.pass();
        if (mc.hitResult instanceof BlockHitResult blockResult && mc.level != null) {
            BlockState blockState = mc.level.getBlockState(blockResult.getBlockPos());
            if (!(blockState.getBlock() instanceof RespawnObeliskBlock)) return EventResult.pass();
            boolean isUpper = false;
            if (!(blockState.getValue(RespawnObeliskBlock.HALF) == DoubleBlockHalf.LOWER)) isUpper = true;
            mc.level.playSound(player, blockResult.getBlockPos(), SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 1, 1);
            ModPackets.CHANNEL.sendToServer(new ScrollWheelPacket(amount, blockResult, isUpper));
            return EventResult.interruptFalse();
        }

        return EventResult.pass();
    }

    protected static void onTextureStitch(TextureAtlas atlas, Consumer<ResourceLocation> spriteAdder) {
        if (!atlas.location().equals(TextureAtlas.LOCATION_BLOCKS)) return;
        spriteAdder.accept(RespawnObeliskBER.RUNES);
    }

    public static void onClientSetup(Minecraft mc) {
        BlockEntityRendererRegistry.register(ModRegistries.RESPAWN_OBELISK_BE.get(), RespawnObeliskBER::new);
    }

    public static void init() {
        ClientRawInputEvent.MOUSE_SCROLLED.register(ClientEvents::onClientScroll);
        ClientTextureStitchEvent.PRE.register(ClientEvents::onTextureStitch);
        ClientLifecycleEvent.CLIENT_SETUP.register(ClientEvents::onClientSetup);
    }
}
