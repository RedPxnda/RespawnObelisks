package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

public class PlayTotemAnimationPacket {
    private final Item item;

    public PlayTotemAnimationPacket(Item item) {
        this.item = item;
    }

    public PlayTotemAnimationPacket(FriendlyByteBuf buffer) {
        this.item = Registry.ITEM.getOptional(ResourceLocation.tryParse(buffer.readUtf())).orElse(Items.TOTEM_OF_UNDYING);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(Registry.ITEM.getKey(item).toString());
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> S2CHandlers.playerTotemAnimation(item));
    }
}
