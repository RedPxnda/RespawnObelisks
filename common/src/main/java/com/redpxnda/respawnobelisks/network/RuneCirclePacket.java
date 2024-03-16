package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import dev.architectury.networking.NetworkManager;
import java.util.function.Supplier;
import net.minecraft.network.PacketByteBuf;

public class RuneCirclePacket {
    private final boolean kill;
    private final int age;
    private final double x;
    private final double y;
    private final double z;

    public RuneCirclePacket(boolean kill, int age, double x, double y, double z) {
        this.kill = kill;
        this.age = age;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public RuneCirclePacket(PacketByteBuf buffer) {
        this.kill = buffer.readBoolean();
        this.age = buffer.readInt();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
    }

    public void toBytes(PacketByteBuf buffer) {
        buffer.writeBoolean(kill);
        buffer.writeInt(age);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> S2CHandlers.setupRuneCircleRenderPacket(kill, age, x, y, z));
    }
}
