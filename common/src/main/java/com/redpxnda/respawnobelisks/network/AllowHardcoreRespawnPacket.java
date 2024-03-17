package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.util.ClientUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Supplier;

public class AllowHardcoreRespawnPacket {
    private final boolean canRespawn;

    public AllowHardcoreRespawnPacket(boolean canRespawn) {
        this.canRespawn = canRespawn;
    }

    public AllowHardcoreRespawnPacket(PacketByteBuf buffer) {
        this.canRespawn = buffer.readBoolean();
    }

    public void toBytes(PacketByteBuf buffer) {
        buffer.writeBoolean(canRespawn);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> ClientUtils.allowHardcoreRespawn = canRespawn);
    }
}
