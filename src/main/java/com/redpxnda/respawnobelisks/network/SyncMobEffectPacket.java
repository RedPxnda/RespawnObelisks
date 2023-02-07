package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.registry.blocks.RespawnObeliskBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class SyncMobEffectPacket {
    private String effect;
    private int amplifier;
    private int duration;

    public SyncMobEffectPacket(String effect, int amplifier, int duration) {
        this.effect = effect;
        this.amplifier = amplifier;
        this.duration = duration;
    }

    public SyncMobEffectPacket(FriendlyByteBuf buffer) {
        effect = buffer.readUtf();
        amplifier = buffer.readInt();
        duration = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(effect);
        buffer.writeInt(amplifier);
        buffer.writeInt(duration);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null) {
                Optional<Holder<MobEffect>> mobEffectHolder = ForgeRegistries.MOB_EFFECTS.getHolder(new ResourceLocation(effect));
                mobEffectHolder.ifPresent(holder -> player.addEffect(new MobEffectInstance(holder.get(), duration, amplifier)));
            }
        });
    }
}
