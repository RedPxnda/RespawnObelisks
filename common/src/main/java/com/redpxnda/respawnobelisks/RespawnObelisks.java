package com.redpxnda.respawnobelisks;

import com.mojang.math.Vector3f;
import com.redpxnda.nucleus.registry.particles.DynamicCameraLockedParticle;
import com.redpxnda.nucleus.registry.particles.DynamicParticle;
import com.redpxnda.nucleus.util.RenderUtil;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCoreListener;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteractionListener;
import com.redpxnda.respawnobelisks.event.ClientEvents;
import com.redpxnda.respawnobelisks.event.CommonEvents;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.theme.RenderTheme;
import com.redpxnda.respawnobelisks.registry.particle.ChargeIndicatorParticle;
import com.teamresourceful.resourcefulconfig.common.config.Configurator;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.server.packs.PackType;

public class RespawnObelisks {
    public static final String MOD_ID = "respawnobelisks";
    public static final Configurator CONFIGURATOR = new Configurator(true);
    
    public static void init() {
        CONFIGURATOR.registerConfig(RespawnObelisksConfig.class);

        ModRegistries.init();
        ModPackets.init();

        CommonEvents.init();

        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            ClientEvents.init();
            RenderTheme.init();
            registerParticleProviders();
        });

        ReloadListenerRegistry.register(PackType.SERVER_DATA, new ObeliskInteractionListener());
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new ObeliskCoreListener());
    }

    private static void registerParticleProviders() {
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
