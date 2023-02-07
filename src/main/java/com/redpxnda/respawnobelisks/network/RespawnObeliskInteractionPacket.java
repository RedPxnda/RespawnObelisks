package com.redpxnda.respawnobelisks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public class RespawnObeliskInteractionPacket {
    private String sound;
    private BlockPos blockPos;
    private boolean isCurse;

    public RespawnObeliskInteractionPacket(String sound, BlockPos blockPos, boolean isCurse) {
        this.sound = sound;
        this.blockPos = blockPos;
        this.isCurse = isCurse;
    }

    public RespawnObeliskInteractionPacket(FriendlyByteBuf buffer) {
        sound = buffer.readUtf();
        blockPos = buffer.readBlockPos();
        isCurse = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(sound);
        buffer.writeBlockPos(blockPos);
        buffer.writeBoolean(isCurse);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientLevel pLevel = Minecraft.getInstance().level;
            if (pLevel != null) {
                Optional<Holder<SoundEvent>> soundHolder = ForgeRegistries.SOUND_EVENTS.getHolder(new ResourceLocation(sound));
                soundHolder.ifPresent(soundEventHolder -> pLevel.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), soundEventHolder.get(), SoundSource.BLOCKS, 1, 1, false));
                if (!isCurse) {
                    for (int i = 0; i < 900; i += 5) {
                        int i1 = i;
                        if (i1 >= 360) i1 = 0;
                        double radians = i * Math.PI / 180;
                        pLevel.addParticle(ParticleTypes.TOTEM_OF_UNDYING, blockPos.getX() + Math.sin(radians) * 0.5 + 0.5, blockPos.getY() + i / 360f, blockPos.getZ() + Math.cos(radians) * 0.5 + 0.5, Math.sin(radians) / 20, 0, Math.cos(radians) / 20);
                    }
                } else {
                    //RandomSource random = RandomSource.create();
                    for (int i = 0; i < 360; i+=3) {
                        double radians = i * Math.PI / 180;
                        pLevel.addParticle(
                                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.NETHER_WART_BLOCK.defaultBlockState()).setPos(blockPos),
                                blockPos.getX()+0.5,
                                blockPos.getY()+0.65,
                                blockPos.getZ()+0.5,
                                Math.sin(radians) / 1.5,
                                0.25,
                                Math.cos(radians) / 1.5
                        );
                    }
                }
            }
        });
    }
}
