package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import com.redpxnda.respawnobelisks.registry.particle.ParticlePack;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class RuneCirclePacket {
    private final int time;
    private final BlockPos pos;

    public RuneCirclePacket(int time, BlockPos pos) {
        this.time = time;
        this.pos = pos;
    }

    public RuneCirclePacket(FriendlyByteBuf buffer) {
        this.time = buffer.readInt();
        this.pos = buffer.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(time);
        buffer.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> S2CHandlers.setupRuneCircleRenderPacket(time, pos));
    }
}
