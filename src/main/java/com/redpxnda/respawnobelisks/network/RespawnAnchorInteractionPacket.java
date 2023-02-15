package com.redpxnda.respawnobelisks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class RespawnAnchorInteractionPacket {
    private BlockPos blockPos;
    private boolean isRun;
    private int charge;

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

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientLevel pLevel = Minecraft.getInstance().level;
            LocalPlayer player = Minecraft.getInstance().player;
            if (pLevel != null && player != null) {
                pLevel.playSound(player, blockPos, SoundEvents.NOTE_BLOCK_BELL, SoundSource.BLOCKS, 1f, 0.75f);
                if (!isRun) {
                    pLevel.addParticle(ParticleTypes.FLASH, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, 0, 0, 0);
                    for (int i = -5; i <= 5; i++) {
                        pLevel.addParticle(ParticleTypes.END_ROD, blockPos.getX() + 0.5, blockPos.getY() + i / 2.5f + 0.5, blockPos.getZ() + 0.5, 0, 0, 0);
                    }
                } else {
                    for (int i = 0; i < 360; i += 20) {
                        double radians = i * Math.PI / 180;
                        pLevel.addParticle(ParticleTypes.EXPLOSION, blockPos.getX() + Math.sin(radians)*charge*2 + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + Math.cos(radians)*charge*2 + 0.5, 0, 0, 0);
                    }
                }
            }
        });
    }
}
