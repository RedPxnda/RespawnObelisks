package com.redpxnda.respawnobelisks.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Packets {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("respawnobelisks", "messages"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void init() {
        INSTANCE.messageBuilder(RespawnObeliskSecondaryInteractionPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RespawnObeliskSecondaryInteractionPacket::new)
                .encoder(RespawnObeliskSecondaryInteractionPacket::toBytes)
                .consumerMainThread(RespawnObeliskSecondaryInteractionPacket::handle)
                .add();
        INSTANCE.messageBuilder(RespawnObeliskInteractionPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RespawnObeliskInteractionPacket::new)
                .encoder(RespawnObeliskInteractionPacket::toBytes)
                .consumerMainThread(RespawnObeliskInteractionPacket::handle)
                .add();
        INSTANCE.messageBuilder(RespawnAnchorInteractionPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RespawnAnchorInteractionPacket::new)
                .encoder(RespawnAnchorInteractionPacket::toBytes)
                .consumerMainThread(RespawnAnchorInteractionPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncMobEffectPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncMobEffectPacket::new)
                .encoder(SyncMobEffectPacket::toBytes)
                .consumerMainThread(SyncMobEffectPacket::handle)
                .add();
        INSTANCE.messageBuilder(ScrollWheelPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ScrollWheelPacket::new)
                .encoder(ScrollWheelPacket::toBytes)
                .consumerMainThread(ScrollWheelPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
