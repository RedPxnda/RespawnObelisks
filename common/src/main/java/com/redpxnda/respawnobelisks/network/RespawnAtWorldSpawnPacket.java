package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import dev.architectury.networking.NetworkManager;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class RespawnAtWorldSpawnPacket {
    private final boolean should;

    public RespawnAtWorldSpawnPacket(boolean should) {
        this.should = should;
    }

    public RespawnAtWorldSpawnPacket(FriendlyByteBuf buffer) {
        this.should = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBoolean(should);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        NetworkManager.PacketContext context = supplier.get();
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer player) {
                SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(player);
                if (facet == null) return;
                facet.willRespawnAtWorldSpawn = should && facet.canChooseWorldSpawn;
            }
        });
    }
}
