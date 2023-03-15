package com.redpxnda.respawnobelisks.fabric;

import com.redpxnda.respawnobelisks.RespawnObelisks;
import net.fabricmc.api.ModInitializer;

public class RespawnObelisksFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RespawnObelisks.init();
    }
}
