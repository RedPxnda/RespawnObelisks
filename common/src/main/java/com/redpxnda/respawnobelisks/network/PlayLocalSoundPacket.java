package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.function.Supplier;

public class PlayLocalSoundPacket {
    private final SoundEvent sound;
    private final float pitch;
    private final float volume;
    private final double x;
    private final double y;
    private final double z;

    public PlayLocalSoundPacket(SoundEvent sound, float pitch, float volume, double x, double y, double z) {
        this.sound = sound;
        this.pitch = pitch;
        this.volume = volume;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PlayLocalSoundPacket(FriendlyByteBuf buffer) {
        this.sound = BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.tryParse(buffer.readUtf())).orElse(SoundEvents.ARMOR_EQUIP_GENERIC);
        this.pitch = buffer.readFloat();
        this.volume = buffer.readFloat();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        ResourceLocation loc;
        buffer.writeUtf((loc = BuiltInRegistries.SOUND_EVENT.getKey(sound)) != null ? loc.toString() : "");
        buffer.writeFloat(pitch);
        buffer.writeFloat(volume);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> S2CHandlers.playLocalClientSound(sound, pitch, volume, x, y, z));
    }
}
