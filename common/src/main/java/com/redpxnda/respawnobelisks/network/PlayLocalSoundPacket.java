package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import dev.architectury.networking.NetworkManager;
import java.util.function.Supplier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

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

    public PlayLocalSoundPacket(PacketByteBuf buffer) {
        this.sound = Registries.SOUND_EVENT.getOrEmpty(Identifier.tryParse(buffer.readString())).orElse(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC);
        this.pitch = buffer.readFloat();
        this.volume = buffer.readFloat();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
    }

    public void toBytes(PacketByteBuf buffer) {
        Identifier loc;
        buffer.writeString((loc = Registries.SOUND_EVENT.getId(sound)) != null ? loc.toString() : "");
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
