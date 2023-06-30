package com.redpxnda.respawnobelisks;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCoreListener;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteractionListener;
import com.redpxnda.respawnobelisks.event.ClientEvents;
import com.redpxnda.respawnobelisks.event.CommonEvents;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.theme.RenderTheme;
import com.teamresourceful.resourcefulconfig.common.config.Configurator;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.server.packs.PackType;

public class RespawnObelisks {
    public static final String MOD_ID = "respawnobelisks";
    public static final Configurator CONFIGURATOR = new Configurator();
    
    public static void init() {
        CONFIGURATOR.registerConfig(RespawnObelisksConfig.class);

        ModRegistries.init();
        ModPackets.init();

        CommonEvents.init();

        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            ClientEvents.init();
            RenderTheme.init();
            ClientEvents.registerParticleProviders();
        });

        ReloadListenerRegistry.register(PackType.SERVER_DATA, new ObeliskInteractionListener());
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new ObeliskCoreListener());
    }
}
