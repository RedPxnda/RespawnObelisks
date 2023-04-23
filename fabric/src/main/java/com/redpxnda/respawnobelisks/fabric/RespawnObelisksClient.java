package com.redpxnda.respawnobelisks.fabric;

import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.item.BoundCompassItem;
import com.redpxnda.respawnobelisks.registry.particle.RuneCircleParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class RespawnObelisksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemProperties.register(ModRegistries.BOUND_COMPASS.get(), new ResourceLocation("angle"), new CompassItemPropertyFunction((level, stack, player) -> BoundCompassItem.isLodestoneCompass(stack) ? BoundCompassItem.getLodestonePosition(stack.getOrCreateTag()) : null));
        ItemProperties.register(ModRegistries.DORMANT_OBELISK.get(), new ResourceLocation(MOD_ID, "dimension"), (stack, level, player, i) -> !stack.hasTag() || !stack.getTag().contains("Dimension") ? 0f : stack.getTag().getFloat("Dimension"));
        ParticleFactoryRegistry.getInstance().register(ModRegistries.RUNE_CIRCLE_PARTICLE.get(), RuneCircleParticle.Provider::new);
    }
}
