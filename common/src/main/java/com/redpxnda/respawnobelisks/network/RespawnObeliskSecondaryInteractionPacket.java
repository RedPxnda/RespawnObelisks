package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import com.redpxnda.respawnobelisks.registry.particle.ParticlePack;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class RespawnObeliskSecondaryInteractionPacket {
    private final int playerId;
    private final ParticlePack pack;
    private final BlockPos blockPos;
    private final boolean isNegative;
    private final boolean isRespawn;

    public RespawnObeliskSecondaryInteractionPacket(int playerId, ParticlePack pack, BlockPos blockPos, boolean isNegative, boolean isRespawn) {
        this.playerId = playerId;
        this.pack = pack;
        this.blockPos = blockPos;
        this.isNegative = isNegative;
        this.isRespawn = isRespawn;
    }

    public RespawnObeliskSecondaryInteractionPacket(FriendlyByteBuf buffer) {
        playerId = buffer.readInt();
        pack = buffer.readEnum(ParticlePack.class);
        blockPos = buffer.readBlockPos();
        isNegative = buffer.readBoolean();
        isRespawn = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(playerId);
        buffer.writeEnum(pack);
        buffer.writeBlockPos(blockPos);
        buffer.writeBoolean(isNegative);
        buffer.writeBoolean(isRespawn);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> {
            S2CHandlers.obeliskInteractionPacket2(playerId, pack, blockPos, isNegative, isRespawn);
        });
    }
}
