package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.network.handler.S2CHandlers;
import dev.architectury.networking.NetworkManager;
import java.util.function.Supplier;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class PlayTotemAnimationPacket {
    private final Item item;

    public PlayTotemAnimationPacket(Item item) {
        this.item = item;
    }

    public PlayTotemAnimationPacket(PacketByteBuf buffer) {
        this.item = Registries.ITEM.getOrEmpty(Identifier.tryParse(buffer.readString())).orElse(Items.TOTEM_OF_UNDYING);
    }

    public void toBytes(PacketByteBuf buffer) {
        buffer.writeString(Registries.ITEM.getId(item).toString());
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        supplier.get().queue(() -> S2CHandlers.playerTotemAnimation(item));
    }
}
