package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import dev.architectury.networking.NetworkManager;
import java.util.function.Supplier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

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

    public PlaySoundPacket(PacketByteBuf buffer) {
        this.sound = Registries.SOUND_EVENT.getOrEmpty(Identifier.tryParse(buffer.readString())).orElse(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC);
        this.pitch = buffer.readFloat();
        this.volume = buffer.readFloat();
    }

    public void toBytes(PacketByteBuf buffer) {
        Identifier loc;
        buffer.writeString((loc = Registries.SOUND_EVENT.getId(sound)) != null ? loc.toString() : "");
        buffer.writeFloat(pitch);
        buffer.writeFloat(volume);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> S2CHandlers.playClientSound(sound, pitch, volume));
    }
}
