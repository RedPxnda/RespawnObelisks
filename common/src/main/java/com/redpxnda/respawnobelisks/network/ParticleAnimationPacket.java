package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import dev.architectury.networking.NetworkManager;
import java.util.function.Supplier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class ParticleAnimationPacket {
    private final String method;
    private final int player;
    private final BlockPos pos;

    public ParticleAnimationPacket(String method, int player, BlockPos pos) {
        this.method = method;
        this.player = player;
        this.pos = pos;
    }

    public ParticleAnimationPacket(PacketByteBuf buffer) {
        this.method = buffer.readString();
        this.player = buffer.readInt();
        this.pos = buffer.readBlockPos();
    }

    public void toBytes(PacketByteBuf buffer) {
        buffer.writeString(method);
        buffer.writeInt(player);
        buffer.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> S2CHandlers.playParticleAnimation(method, player, pos));
    }
}
