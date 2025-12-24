package com.redpxnda.respawnobelisks.network;

import com.redpxnda.nucleus.facet.network.clientbound.FacetSyncPacket;
import com.redpxnda.nucleus.util.ByteBufUtil;
import com.redpxnda.respawnobelisks.RespawnObelisks;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SetPriorityChangerPacket extends FacetSyncPacket<CompoundTag, SecondarySpawnPoints> {
    private final Map<SpawnPoint, ItemStack> cachedItems;
    private final Map<SpawnPoint, Block> cachedBlocks;

    @Override
    public void send(ServerPlayer player) {
        ModPackets.CHANNEL.sendToPlayer(player, this);
    }

    @Override
    public void send(Iterable<ServerPlayer> players) {
        ModPackets.CHANNEL.sendToPlayers(players, this);
    }

    public SetPriorityChangerPacket(Entity target, SecondarySpawnPoints facet) {
        super(target, SecondarySpawnPoints.KEY, facet);
        cachedItems = new HashMap<>();
        cachedBlocks = new HashMap<>();
        for (SpawnPoint point : facet.points) {
            Block block = target.getServer().getLevel(point.dimension()).getBlockState(point.pos()).getBlock();
            ItemStack stack = block.asItem().getDefaultInstance();
            if (target.getServer().getLevel(point.dimension()).getBlockEntity(point.pos()) instanceof RespawnObeliskBlockEntity blockEntity) {
                ItemStack coreStack = blockEntity.getCoreInstance().stack();
                if (coreStack.getItem().getDefaultInstance().getHoverName().equals(coreStack.getHoverName()))
                    stack.setHoverName(Component.literal("Respawn Obelisk"));
                else stack.setHoverName(coreStack.getHoverName());
            }
            cachedItems.put(point, stack);
            if (RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.secondarySpawnBlocksAsWhitelist == RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.secondarySpawnBlockBlacklist.contains(block))
                cachedBlocks.put(point, block);
        }
    }

    public SetPriorityChangerPacket(FriendlyByteBuf buffer) {
        super(buffer);
        cachedItems = new HashMap<>();
        cachedBlocks = new HashMap<>();

        int mapSize = buffer.readInt();
        for (int i = 0; i < mapSize; i++) {
            int tempX = buffer.readInt();
            int tempY = buffer.readInt();
            int tempZ = buffer.readInt();
            ResourceKey<Level> tempWorld = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buffer.readUtf()));

            SpawnPoint spawnPoint = new SpawnPoint(tempWorld, new BlockPos(tempX, tempY, tempZ), 0, false);
            Tag tag = ByteBufUtil.readTag(buffer);
            ItemStack item;
            if (!(tag instanceof CompoundTag compound)) {
                RespawnObelisks.getLogger().error("Nbt from byte buffer is not a compound tag for an ItemStack!");
                item = ItemStack.EMPTY;
            } else item = ItemStack.of(compound);

            cachedItems.put(spawnPoint, item);
        }

        mapSize = buffer.readInt();
        for (int i = 0; i < mapSize; i++) {
            int tempX = buffer.readInt();
            int tempY = buffer.readInt();
            int tempZ = buffer.readInt();
            ResourceKey<Level> tempWorld = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buffer.readUtf()));

            SpawnPoint spawnPoint = new SpawnPoint(tempWorld, new BlockPos(tempX, tempY, tempZ), 0, false);
            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(buffer.readUtf()));

            cachedBlocks.put(spawnPoint, block);
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        super.toBuffer(buffer);
        buffer.writeInt(cachedItems.size());
        cachedItems.forEach((point, item) -> {
            buffer.writeInt(point.pos().getX());
            buffer.writeInt(point.pos().getY());
            buffer.writeInt(point.pos().getZ());
            buffer.writeUtf(point.dimension().location().toString());
            ByteBufUtil.writeTag(item.save(new CompoundTag()), buffer);
            //buffer.writeUtf(BuiltInRegistries.ITEM.getKey(item).toString());
        });

        buffer.writeInt(cachedBlocks.size());
        cachedBlocks.forEach((point, block) -> {
            buffer.writeInt(point.pos().getX());
            buffer.writeInt(point.pos().getY());
            buffer.writeInt(point.pos().getZ());
            buffer.writeUtf(point.dimension().location().toString());
            buffer.writeUtf(BuiltInRegistries.BLOCK.getKey(block).toString());
        });
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        NetworkManager.PacketContext context = supplier.get();
        supplier.get().queue(() -> {
            super.handle(context);
            ClientUtils.cachedSpawnPointBlocks = cachedBlocks;
            ClientUtils.cachedSpawnPointItems = cachedItems;
            ClientUtils.hasLookedAwayFromPriorityChanger = false;
        });
    }
}
