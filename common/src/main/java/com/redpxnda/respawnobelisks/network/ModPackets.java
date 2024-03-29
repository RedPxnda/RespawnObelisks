package com.redpxnda.respawnobelisks.network;

import dev.architectury.networking.NetworkChannel;
import net.minecraft.util.Identifier;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class ModPackets {
    public static final NetworkChannel CHANNEL = NetworkChannel.create(new Identifier(MOD_ID, "main"));

    public static void init() {
        CHANNEL.register(SyncEffectsPacket.class, SyncEffectsPacket::toBytes, SyncEffectsPacket::new, SyncEffectsPacket::handle);
        CHANNEL.register(RespawnAnchorInteractionPacket.class, RespawnAnchorInteractionPacket::toBytes, RespawnAnchorInteractionPacket::new, RespawnAnchorInteractionPacket::handle);
        CHANNEL.register(ParticleAnimationPacket.class, ParticleAnimationPacket::toBytes, ParticleAnimationPacket::new, ParticleAnimationPacket::handle);
        CHANNEL.register(ScrollWheelPacket.class, ScrollWheelPacket::toBytes, ScrollWheelPacket::new, ScrollWheelPacket::handle);
        CHANNEL.register(PlaySoundPacket.class, PlaySoundPacket::toBytes, PlaySoundPacket::new, PlaySoundPacket::handle);
        CHANNEL.register(PlayLocalSoundPacket.class, PlayLocalSoundPacket::toBytes, PlayLocalSoundPacket::new, PlayLocalSoundPacket::handle);
        CHANNEL.register(PlayTotemAnimationPacket.class, PlayTotemAnimationPacket::toBytes, PlayTotemAnimationPacket::new, PlayTotemAnimationPacket::handle);
        CHANNEL.register(RuneCirclePacket.class, RuneCirclePacket::toBytes, RuneCirclePacket::new, RuneCirclePacket::handle);
        CHANNEL.register(SetPriorityChangerPacket.class, SetPriorityChangerPacket::toBytes, SetPriorityChangerPacket::new, SetPriorityChangerPacket::handle);
        CHANNEL.register(AllowHardcoreRespawnPacket.class, AllowHardcoreRespawnPacket::toBytes, AllowHardcoreRespawnPacket::new, AllowHardcoreRespawnPacket::handle);
        CHANNEL.register(FinishPriorityChangePacket.class, FinishPriorityChangePacket::toBytes, FinishPriorityChangePacket::new, FinishPriorityChangePacket::handle);
        CHANNEL.register(RespawnAtWorldSpawnPacket.class, RespawnAtWorldSpawnPacket::toBytes, RespawnAtWorldSpawnPacket::new, RespawnAtWorldSpawnPacket::handle);
    }
}
