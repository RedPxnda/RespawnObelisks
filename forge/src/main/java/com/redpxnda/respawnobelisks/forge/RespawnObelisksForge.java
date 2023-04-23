package com.redpxnda.respawnobelisks.forge;

import com.redpxnda.respawnobelisks.forge.compat.CuriosCompat;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.item.BoundCompassItem;
import com.redpxnda.respawnobelisks.registry.particle.RuneCircleParticle;
import dev.architectury.platform.Platform;
import dev.architectury.platform.forge.EventBuses;
import com.redpxnda.respawnobelisks.RespawnObelisks;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

@Mod(MOD_ID)
public class RespawnObelisksForge {
    public RespawnObelisksForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        RespawnObelisks.init();

        if (Platform.isModLoaded("curios")) {
            MinecraftForge.EVENT_BUS.addListener(CuriosCompat::onDropRules);
        }
    }

    public static class ClientEvents {
        @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
        public static class ModBus {
            @SubscribeEvent
            public static void onClientSetup(FMLClientSetupEvent event) {
                event.enqueueWork(() -> {
                    ItemProperties.register(ModRegistries.BOUND_COMPASS.get(), new ResourceLocation("angle"), new CompassItemPropertyFunction((level, stack, player) -> BoundCompassItem.isLodestoneCompass(stack) ? BoundCompassItem.getLodestonePosition(stack.getOrCreateTag()) : null));
                    ItemProperties.register(ModRegistries.DORMANT_OBELISK.get(), new ResourceLocation(MOD_ID, "dimension"), (stack, level, player, i) -> !stack.hasTag() || !stack.getTag().contains("Dimension") ? 0f : stack.getTag().getFloat("Dimension"));
                });
            }

            @SubscribeEvent
            public static void onParticleProvidersRegistry(RegisterParticleProvidersEvent event) {
                event.register(ModRegistries.RUNE_CIRCLE_PARTICLE.get(), RuneCircleParticle.Provider::new);
            }
        }
    }
}
