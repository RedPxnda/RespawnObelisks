package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import com.redpxnda.respawnobelisks.registry.particle.packs.ParticlePack;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class FirePackMethodPacket {
    private final String method;
    private final int player;
    private final ParticlePack pack;
    private final BlockPos pos;

    public FirePackMethodPacket(String method, int player, ParticlePack pack, BlockPos pos) {
        this.method = method;
        this.player = player;
        this.pack = pack;
        this.pos = pos;
    }

    public FirePackMethodPacket(FriendlyByteBuf buffer) {
        this.method = buffer.readUtf();
        this.player = buffer.readInt();
        this.pack = buffer.readEnum(ParticlePack.class);
        this.pos = buffer.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(method);
        buffer.writeInt(player);
        buffer.writeEnum(pack);
        buffer.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> S2CHandlers.firePackMethodPacket(method, player, pack, pos));
    }
}
