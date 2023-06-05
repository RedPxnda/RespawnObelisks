package com.redpxnda.respawnobelisks.event;

import com.mojang.math.Vector3f;
import com.redpxnda.nucleus.registry.particles.DynamicParticle;
import com.redpxnda.nucleus.util.RenderUtil;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBER;
import com.redpxnda.respawnobelisks.registry.particle.ChargeIndicatorParticle;
import com.redpxnda.respawnobelisks.util.RenderUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.*;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
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
/*    protected static EventResult onClientScroll(Minecraft mc, double amount) {
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
    }*/

    protected static void onTextureStitch(TextureAtlas atlas, Consumer<ResourceLocation> spriteAdder) {
        if (!atlas.location().equals(TextureAtlas.LOCATION_BLOCKS)) return;
        spriteAdder.accept(RespawnObeliskBER.RUNES);
        RenderUtils.getThemeTextures().forEach((str, loc) -> spriteAdder.accept(loc));
    }

    public static void onClientSetup(Minecraft mc) {
        BlockEntityRendererRegistry.register(ModRegistries.RESPAWN_OBELISK_BE.get(), RespawnObeliskBER::new);
//        if (Platform.isFabric()) // idk this is super goofy: libraries say the `register` method is public, I even AW'd it, but still I get errors :P
//            ItemPropertiesAccessor.register(ModRegistries.BOUND_COMPASS.get(), new ResourceLocation("angle"), new CompassItemPropertyFunction((level, stack, player) -> BoundCompassItem.isLodestoneCompass(stack) ? BoundCompassItem.getLodestonePosition(stack.getOrCreateTag()) : null));
    }

    public static void init() {
        //ClientRawInputEvent.MOUSE_SCROLLED.register(ClientEvents::onClientScroll);
        ClientTextureStitchEvent.PRE.register(ClientEvents::onTextureStitch);
        ClientLifecycleEvent.CLIENT_SETUP.register(ClientEvents::onClientSetup);
    }

    public static void registerParticleProviders() {
        ParticleProviderRegistry.register(ModRegistries.DEPLETE_RING_PARTICLE, set -> new DynamicParticle.Provider(set,
                setup -> setup.setLifetime(50),
                tick -> {
                    tick.scale+=0.25/(tick.getAge()/4f + 1);
                    if (tick.getAge() > 38)
                        tick.alpha-=0.05;
                },
                (render, vecs) -> RenderUtil.rotateVectors(vecs, Vector3f.XP.rotationDegrees(90f))
        ));
        ParticleProviderRegistry.register(ModRegistries.CHARGE_INDICATOR_PARTICLE, ChargeIndicatorParticle.Provider::new);
    }
}
