package com.redpxnda.respawnobelisks.forge;

import dev.architectury.platform.forge.EventBuses;
import com.redpxnda.respawnobelisks.RespawnObelisks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RespawnObelisks.MOD_ID)
public class RespawnObelisksForge {
    public RespawnObelisksForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(RespawnObelisks.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        RespawnObelisks.init();
    }
}
