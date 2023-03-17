package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import com.redpxnda.respawnobelisks.registry.particle.ParticlePack;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;
import java.util.function.Supplier;

public class RespawnObeliskInteractionPacket {
    private final ParticlePack pack;
    private final BlockPos blockPos;
    private final boolean isCurse;

    public RespawnObeliskInteractionPacket(ParticlePack pack, BlockPos blockPos, boolean isCurse) {
        this.pack = pack;
        this.blockPos = blockPos;
        this.isCurse = isCurse;
    }

    public RespawnObeliskInteractionPacket(FriendlyByteBuf buffer) {
        pack = buffer.readEnum(ParticlePack.class);
        blockPos = buffer.readBlockPos();
        isCurse = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeEnum(pack);
        buffer.writeBlockPos(blockPos);
        buffer.writeBoolean(isCurse);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> {
            S2CHandlers.obeliskInteractionPacket1(blockPos, pack, isCurse);
        });
    }
}
