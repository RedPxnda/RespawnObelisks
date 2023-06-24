package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.function.Supplier;

public class PlaySoundPacket {
    private final SoundEvent sound;
    private final float pitch;
    private final float volume;

    public PlaySoundPacket(SoundEvent sound, float pitch, float volume) {
        //this.sound = Registry.SOUND_EVENT.getOptional(ResourceLocation.tryParse(sound)).orElse(SoundEvents.ARMOR_EQUIP_GENERIC);
        this.sound = sound;
        this.pitch = pitch;
        this.volume = volume;
    }

    public PlaySoundPacket(FriendlyByteBuf buffer) {
        this.sound = Registry.SOUND_EVENT.getOptional(ResourceLocation.tryParse(buffer.readUtf())).orElse(SoundEvents.ARMOR_EQUIP_GENERIC);
        this.pitch = buffer.readFloat();
        this.volume = buffer.readFloat();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        ResourceLocation loc;
        buffer.writeUtf((loc = Registry.SOUND_EVENT.getKey(sound)) != null ? loc.toString() : "");
        buffer.writeFloat(pitch);
        buffer.writeFloat(volume);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> S2CHandlers.playClientSound(sound, pitch, volume));
    }
}
