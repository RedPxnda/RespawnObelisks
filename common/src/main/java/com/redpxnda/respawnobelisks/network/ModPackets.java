package com.redpxnda.respawnobelisks.network;

import dev.architectury.networking.NetworkChannel;
import net.minecraft.resources.ResourceLocation;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class ModPackets {
    public static final NetworkChannel CHANNEL = NetworkChannel.create(new ResourceLocation(MOD_ID, "main"));

    public static void init() {
        CHANNEL.register(SyncEffectsPacket.class, SyncEffectsPacket::toBytes, SyncEffectsPacket::new, SyncEffectsPacket::handle);
        CHANNEL.register(RespawnAnchorInteractionPacket.class, RespawnAnchorInteractionPacket::toBytes, RespawnAnchorInteractionPacket::new, RespawnAnchorInteractionPacket::handle);
        CHANNEL.register(RespawnObeliskInteractionPacket.class, RespawnObeliskInteractionPacket::toBytes, RespawnObeliskInteractionPacket::new, RespawnObeliskInteractionPacket::handle);
        CHANNEL.register(RespawnObeliskSecondaryInteractionPacket.class, RespawnObeliskSecondaryInteractionPacket::toBytes, RespawnObeliskSecondaryInteractionPacket::new, RespawnObeliskSecondaryInteractionPacket::handle);
        CHANNEL.register(ScrollWheelPacket.class, ScrollWheelPacket::toBytes, ScrollWheelPacket::new, ScrollWheelPacket::handle);
        CHANNEL.register(PlaySoundPacket.class, PlaySoundPacket::toBytes, PlaySoundPacket::new, PlaySoundPacket::handle);
    }
}
