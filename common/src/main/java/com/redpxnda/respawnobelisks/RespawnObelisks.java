package com.redpxnda.respawnobelisks;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.event.ClientEvents;
import com.redpxnda.respawnobelisks.event.CommonEvents;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.teamresourceful.resourcefulconfig.common.config.Configurator;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;

public class RespawnObelisks {
    public static final String MOD_ID = "respawnobelisks";
    public static final Configurator CONFIGURATOR = new Configurator(true);
    
    public static void init() {
        CONFIGURATOR.registerConfig(RespawnObelisksConfig.class);

        ModRegistries.init();
        ModPackets.init();

        CommonEvents.init();

        EnvExecutor.runInEnv(Env.CLIENT, () -> ClientEvents::init);
    }
}
