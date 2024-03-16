package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import dev.architectury.networking.NetworkManager;
import java.util.function.Supplier;
import net.minecraft.network.PacketByteBuf;

public class SyncEffectsPacket {
    private final int amplifier;
    private final int duration;

    public SyncEffectsPacket(int amplifier, int duration) {
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public SyncEffectsPacket(PacketByteBuf buffer) {
        amplifier = buffer.readInt();
        duration = buffer.readInt();
    }

    public void toBytes(PacketByteBuf buffer) {
        buffer.writeInt(amplifier);
        buffer.writeInt(duration);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> {
            S2CHandlers.syncEffectsPacket(amplifier, duration);
        });
    }
}
