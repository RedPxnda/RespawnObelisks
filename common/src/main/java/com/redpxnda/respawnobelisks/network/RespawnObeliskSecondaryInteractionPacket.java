package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
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
    private final String sound;
    private final BlockPos blockPos;
    private final boolean isNegative;
    private final boolean isRespawn;

    public RespawnObeliskSecondaryInteractionPacket(String sound, BlockPos blockPos, boolean isNegative, boolean isRespawn) {
        this.sound = sound;
        this.blockPos = blockPos;
        this.isNegative = isNegative;
        this.isRespawn = isRespawn;
    }

    public RespawnObeliskSecondaryInteractionPacket(FriendlyByteBuf buffer) {
        sound = buffer.readUtf();
        blockPos = buffer.readBlockPos();
        isNegative = buffer.readBoolean();
        isRespawn = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(sound);
        buffer.writeBlockPos(blockPos);
        buffer.writeBoolean(isNegative);
        buffer.writeBoolean(isRespawn);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> {
            S2CHandlers.obeliskInteractionPacket2(sound, blockPos, isNegative, isRespawn);
        });
    }
}
