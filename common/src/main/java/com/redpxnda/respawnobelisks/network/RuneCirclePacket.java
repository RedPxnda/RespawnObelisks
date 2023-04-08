package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class RuneCirclePacket {
    private final int age;
    private final double x;
    private final double y;
    private final double z;

    public RuneCirclePacket(int age, double x, double y, double z) {
        this.age = age;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public RuneCirclePacket(FriendlyByteBuf buffer) {
        this.age = buffer.readInt();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(age);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> S2CHandlers.setupRuneCircleRenderPacket(age, x, y, z));
    }
}
