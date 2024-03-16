package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.util.ClientUtils;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import dev.architectury.networking.NetworkManager;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SetPriorityChangerPacket {
    private final SpawnPoint target;
    private final List<SpawnPoint> points;
    private final Map<SpawnPoint, Item> cachedItems;

    public SetPriorityChangerPacket(SpawnPoint target, List<SpawnPoint> points, MinecraftServer server) {
        this.target = target;
        this.points = points;
        this.cachedItems = new HashMap<>();

        for (SpawnPoint point : this.points) {
            cachedItems.put(point, server.getWorld(point.dimension()).getBlockState(point.pos()).getBlock().asItem());
        }
    }

    public SetPriorityChangerPacket(PacketByteBuf buffer) {
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, new Identifier(buffer.readString()));
        target = new SpawnPoint(world, new BlockPos(x, y, z), 0, false);

        points = new ArrayList<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            int tempX = buffer.readInt();
            int tempY = buffer.readInt();
            int tempZ = buffer.readInt();
            RegistryKey<World> tempWorld = RegistryKey.of(RegistryKeys.WORLD, new Identifier(buffer.readString()));
            points.add(new SpawnPoint(tempWorld, new BlockPos(tempX, tempY, tempZ), 0, false));
        }

        cachedItems = new HashMap<>();
        int mapSize = buffer.readInt();
        for (int i = 0; i < mapSize; i++) {
            int tempX = buffer.readInt();
            int tempY = buffer.readInt();
            int tempZ = buffer.readInt();
            RegistryKey<World> tempWorld = RegistryKey.of(RegistryKeys.WORLD, new Identifier(buffer.readString()));

            SpawnPoint spawnPoint = new SpawnPoint(tempWorld, new BlockPos(tempX, tempY, tempZ), 0, false);
            Item item = Registries.ITEM.get(new Identifier(buffer.readString()));

            cachedItems.put(spawnPoint, item);
        }
    }

    public void toBytes(PacketByteBuf buffer) {
        buffer.writeInt(target.pos().getX());
        buffer.writeInt(target.pos().getY());
        buffer.writeInt(target.pos().getZ());
        buffer.writeString(target.dimension().getValue().toString());

        buffer.writeInt(points.size());
        for (SpawnPoint point : points) {
            buffer.writeInt(point.pos().getX());
            buffer.writeInt(point.pos().getY());
            buffer.writeInt(point.pos().getZ());
            buffer.writeString(point.dimension().getValue().toString());
        }

        buffer.writeInt(cachedItems.size());
        cachedItems.forEach((point, item) -> {
            buffer.writeInt(point.pos().getX());
            buffer.writeInt(point.pos().getY());
            buffer.writeInt(point.pos().getZ());
            buffer.writeString(point.dimension().getValue().toString());
            buffer.writeString(Registries.ITEM.getId(item).toString());
        });
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        NetworkManager.PacketContext context = supplier.get();
        supplier.get().queue(() -> {
            ClientUtils.priorityChangerIndex = points.size()-1;
            ClientUtils.focusedPriorityChanger = target;
            ClientUtils.cachedSpawnPointItems = cachedItems;
            ClientUtils.allCachedSpawnPoints = points;
        });
    }
}
