package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class RespawnAnchorInteractionPacket {
    private final BlockPos blockPos;
    private final boolean isRun;
    private final int charge;

    public RespawnAnchorInteractionPacket(BlockPos blockPos, boolean isRun, int charge) {
        this.blockPos = blockPos;
        this.isRun = isRun;
        this.charge = charge;
    }

    public RespawnAnchorInteractionPacket(FriendlyByteBuf buffer) {
        blockPos = buffer.readBlockPos();
        isRun = buffer.readBoolean();
        charge = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
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
