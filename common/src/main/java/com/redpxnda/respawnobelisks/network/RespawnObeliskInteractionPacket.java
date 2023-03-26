package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import com.redpxnda.respawnobelisks.registry.particle.ParticlePack;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;
import java.util.function.Supplier;

public class RespawnObeliskInteractionPacket {
    private final int playerId;
    private final ParticlePack pack;
    private final BlockPos blockPos;
    private final boolean isCurse;

    public RespawnObeliskInteractionPacket(int playerId, ParticlePack pack, BlockPos blockPos, boolean isCurse) {
        this.playerId = playerId;
        this.pack = pack;
        this.blockPos = blockPos;
        this.isCurse = isCurse;
    }

    public RespawnObeliskInteractionPacket(FriendlyByteBuf buffer) {
        playerId = buffer.readInt();
        pack = buffer.readEnum(ParticlePack.class);
        blockPos = buffer.readBlockPos();
        isCurse = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(playerId);
        buffer.writeEnum(pack);
        buffer.writeBlockPos(blockPos);
        buffer.writeBoolean(isCurse);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> {
            S2CHandlers.obeliskInteractionPacket1(playerId, blockPos, pack, isCurse);
        });
    }
}
