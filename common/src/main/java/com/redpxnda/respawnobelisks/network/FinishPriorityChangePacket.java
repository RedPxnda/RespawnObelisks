package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FinishPriorityChangePacket {
    private final List<SpawnPoint> newOrder;

    public FinishPriorityChangePacket(List<SpawnPoint> newOrder) {
        this.newOrder = newOrder;
    }

    public FinishPriorityChangePacket(PacketByteBuf buffer) {
        this.newOrder = new ArrayList<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            int tempX = buffer.readInt();
            int tempY = buffer.readInt();
            int tempZ = buffer.readInt();
            RegistryKey<World> tempWorld = RegistryKey.of(RegistryKeys.WORLD, new Identifier(buffer.readString()));

            SpawnPoint spawnPoint = new SpawnPoint(tempWorld, new BlockPos(tempX, tempY, tempZ), 0, false);
            newOrder.add(spawnPoint);
        }
    }

    public void toBytes(PacketByteBuf buffer) {
        buffer.writeInt(newOrder.size());
        for (SpawnPoint point : newOrder) {
            buffer.writeInt(point.pos().getX());
            buffer.writeInt(point.pos().getY());
            buffer.writeInt(point.pos().getZ());
            buffer.writeString(point.dimension().getValue().toString());
        }
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        NetworkManager.PacketContext context = supplier.get();
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayerEntity player) {
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
