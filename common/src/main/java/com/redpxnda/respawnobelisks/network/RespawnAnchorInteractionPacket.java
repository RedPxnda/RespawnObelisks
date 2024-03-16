package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import dev.architectury.networking.NetworkManager;
import java.util.function.Supplier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class RespawnAnchorInteractionPacket {
    private final BlockPos blockPos;
    private final boolean isRun;
    private final int charge;

    public RespawnAnchorInteractionPacket(BlockPos blockPos, boolean isRun, int charge) {
        this.blockPos = blockPos;
        this.isRun = isRun;
        this.charge = charge;
    }

    public RespawnAnchorInteractionPacket(PacketByteBuf buffer) {
        blockPos = buffer.readBlockPos();
        isRun = buffer.readBoolean();
        charge = buffer.readInt();
    }

    public void toBytes(PacketByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeBoolean(isRun);
        buffer.writeInt(charge);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> {
            S2CHandlers.respawnAnchorPacket(blockPos, charge, isRun);
        });
    }
}
