package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import dev.architectury.networking.NetworkManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class FinishPriorityChangePacket {
    private final List<SpawnPoint> newOrder;

    public FinishPriorityChangePacket(List<SpawnPoint> newOrder) {
        this.newOrder = newOrder;
    }

    public FinishPriorityChangePacket(FriendlyByteBuf buffer) {
        this.newOrder = new ArrayList<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            int tempX = buffer.readInt();
            int tempY = buffer.readInt();
            int tempZ = buffer.readInt();
            ResourceKey<Level> tempWorld = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buffer.readUtf()));

            SpawnPoint spawnPoint = new SpawnPoint(tempWorld, new BlockPos(tempX, tempY, tempZ), 0, false);
            newOrder.add(spawnPoint);
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(newOrder.size());
        for (SpawnPoint point : newOrder) {
            buffer.writeInt(point.pos().getX());
            buffer.writeInt(point.pos().getY());
            buffer.writeInt(point.pos().getZ());
            buffer.writeUtf(point.dimension().location().toString());
        }
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        NetworkManager.PacketContext context = supplier.get();
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer player) {
                SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(player);
                if (facet == null || (!RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.allowPriorityShifting && !facet.canChooseRespawn)) return;
                facet.reorderingTarget = null;

                List<SpawnPoint> finalList = new ArrayList<>();
                for (SpawnPoint point : newOrder) {
                    int index = facet.points.indexOf(point); // ensure the client isnt fucking with us
                    if (index == -1) return;

                    SpawnPoint real = facet.points.get(index);
                    if (real == null) return;

                    finalList.add(real);
                }

                facet.points.clear();
                facet.points.addAll(finalList);
            }
        });
    }
}
