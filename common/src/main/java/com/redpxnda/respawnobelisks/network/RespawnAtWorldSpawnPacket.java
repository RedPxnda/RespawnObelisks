package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Supplier;

public class RespawnAtWorldSpawnPacket {
    private final boolean should;

    public RespawnAtWorldSpawnPacket(boolean should) {
        this.should = should;
    }

    public RespawnAtWorldSpawnPacket(PacketByteBuf buffer) {
        this.should = buffer.readBoolean();
    }

    public void toBytes(PacketByteBuf buffer) {
        buffer.writeBoolean(should);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        NetworkManager.PacketContext context = supplier.get();
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayerEntity player) {
                SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(player);
                if (facet == null) return;
                facet.willRespawnAtWorldSpawn = should && facet.canChooseWorldSpawn;
            }
        });
    }
}
