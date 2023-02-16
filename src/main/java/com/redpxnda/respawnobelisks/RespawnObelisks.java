package com.redpxnda.respawnobelisks;

import com.mojang.logging.LogUtils;
import com.redpxnda.respawnobelisks.config.ServerConfig;
import com.redpxnda.respawnobelisks.event.RespawnObeliskEvents;
import com.redpxnda.respawnobelisks.network.Packets;
import com.redpxnda.respawnobelisks.registry.Registry;
import net.minecraft.data.worldgen.Structures;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(RespawnObelisks.MODID)
public class RespawnObelisks {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "respawnobelisks";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public RespawnObelisks() {
//        // Loading player list class for mixins
//        Class<?> loadingPlayerList = PlayerList.class;

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::setup);

        Registry.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, "respawnobelisks-server.toml");

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(RespawnObeliskEvents.class);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(Packets::init);
    }
}
