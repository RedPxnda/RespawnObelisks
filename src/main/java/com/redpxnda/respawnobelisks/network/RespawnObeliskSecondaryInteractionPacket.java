package com.redpxnda.respawnobelisks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class RespawnObeliskSecondaryInteractionPacket {
    private String sound;
    private BlockPos blockPos;
    private boolean isNegative;
    private boolean isRespawn;

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

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientLevel pLevel = Minecraft.getInstance().level;
            if (pLevel != null) {
                Optional<Holder<SoundEvent>> soundHolder = ForgeRegistries.SOUND_EVENTS.getHolder(new ResourceLocation(sound));
                soundHolder.ifPresent(soundEventHolder -> pLevel.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), soundEventHolder.get(), SoundSource.BLOCKS, 1, 1, false));
                if (isRespawn) return;
                if (!isNegative) {
                    for (int i = 0; i < 360; i += 5) {
                        double radians = i * Math.PI / 180;
                        pLevel.addParticle(ParticleTypes.END_ROD, blockPos.getX() + Math.sin(radians)*3 + 0.5, blockPos.getY() + 0.7, blockPos.getZ() + Math.cos(radians)*3 + 0.5, -Math.sin(radians)/5, 0.05, -Math.cos(radians)/5);
                    }
                } else {
                    for (int i = 0; i < 360; i += 5) {
                        double radians = i * Math.PI / 180;
                        pLevel.addParticle(ParticleTypes.END_ROD, blockPos.getX() + Math.sin(radians)*0.5 + 0.5, blockPos.getY() + 1.6, blockPos.getZ() + Math.cos(radians)*0.5 + 0.5, Math.sin(radians)/5, -0.05, Math.cos(radians)/5);
                    }
                }
            }
        });
    }
}
