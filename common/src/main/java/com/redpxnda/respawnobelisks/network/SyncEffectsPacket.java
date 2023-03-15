package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.function.Supplier;

public class SyncEffectsPacket {
    private final int amplifier;
    private final int duration;

    public SyncEffectsPacket(int amplifier, int duration) {
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public SyncEffectsPacket(FriendlyByteBuf buffer) {
        amplifier = buffer.readInt();
        duration = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(amplifier);
        buffer.writeInt(duration);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> {
            S2CHandlers.syncEffectsPacket(amplifier, duration);
        });
    }
}
