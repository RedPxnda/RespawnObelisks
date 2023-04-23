package com.redpxnda.respawnobelisks.fabric;

import com.redpxnda.respawnobelisks.RespawnObelisks;
import com.redpxnda.respawnobelisks.fabric.compat.TrinketsCompat;
import dev.architectury.platform.Platform;
import net.fabricmc.api.ModInitializer;

public class RespawnObelisksFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RespawnObelisks.init();
        if (Platform.isModLoaded("trinkets")) TrinketsCompat.init();
    }
}
