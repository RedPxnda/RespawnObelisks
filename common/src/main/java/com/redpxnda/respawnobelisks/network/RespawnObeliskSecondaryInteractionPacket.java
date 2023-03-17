package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import com.redpxnda.respawnobelisks.registry.particle.ParticlePack;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Supplier;

public class RespawnObeliskSecondaryInteractionPacket {
    private final ParticlePack pack;
    private final BlockPos blockPos;
    private final boolean isNegative;
    private final boolean isRespawn;

    public RespawnObeliskSecondaryInteractionPacket(ParticlePack pack, BlockPos blockPos, boolean isNegative, boolean isRespawn) {
        this.pack = pack;
        this.blockPos = blockPos;
        this.isNegative = isNegative;
        this.isRespawn = isRespawn;
    }

    public RespawnObeliskSecondaryInteractionPacket(FriendlyByteBuf buffer) {
        pack = buffer.readEnum(ParticlePack.class);
        blockPos = buffer.readBlockPos();
        isNegative = buffer.readBoolean();
        isRespawn = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeEnum(pack);
        buffer.writeBlockPos(blockPos);
        buffer.writeBoolean(isNegative);
        buffer.writeBoolean(isRespawn);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> {
            S2CHandlers.obeliskInteractionPacket2(pack, blockPos, isNegative, isRespawn);
        });
    }
}
