package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.util.ClientUtils;
import dev.architectury.networking.NetworkManager;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;

public class AllowHardcoreRespawnPacket {
    private final boolean canRespawn;

    public AllowHardcoreRespawnPacket(boolean canRespawn) {
        this.canRespawn = canRespawn;
    }

    public AllowHardcoreRespawnPacket(FriendlyByteBuf buffer) {
        this.canRespawn = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBoolean(canRespawn);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> ClientUtils.allowHardcoreRespawn = canRespawn);
    }
}
