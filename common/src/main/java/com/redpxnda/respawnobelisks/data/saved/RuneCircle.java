package com.redpxnda.respawnobelisks.data.saved;

import com.redpxnda.respawnobelisks.config.TeleportConfig;
import com.redpxnda.respawnobelisks.mixin.LivingEntityAccessor;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.PlayLocalSoundPacket;
import com.redpxnda.respawnobelisks.network.RuneCirclePacket;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class RuneCircle {
    public int tick;
    public int idleTick = 0;
    public final int maxTick = 100;
    public final UUID playerUUID;
    public final BlockPos pos;
    public final BlockPos target;
    public final Vec3 startPos;
    public boolean stopped = false;

    public RuneCircle(ServerLevel level, int tick, UUID player, BlockPos pos, BlockPos target, double x, double y, double z) {
        this.tick = tick;
        this.playerUUID = player;
        this.pos = pos;
        RespawnObeliskBlockEntity blockEntity = (RespawnObeliskBlockEntity)level.getBlockEntity(pos);
        blockEntity.hasTeleportingEntity = true;
        blockEntity.syncWithClient();
        this.target = target;
        this.startPos = new Vec3(x, y, z);
        List<ServerPlayer> toContact = level.getPlayers(p -> getAABB().contains(p.getX(), p.getY(), p.getZ()));
        ModPackets.CHANNEL.sendToPlayers(toContact, new RuneCirclePacket(tick, x, y, z));
    }

    public RuneCircle(ServerLevel level, ServerPlayer player, BlockPos pos, BlockPos target, double x, double y, double z) {
        this(level, 0, player.getUUID(), pos, target, x, y, z);
        List<ServerPlayer> players = level.getPlayers(p -> getAABB().contains(p.getX(), p.getY(), p.getZ()));
        ModPackets.CHANNEL.sendToPlayers(players, new PlayLocalSoundPacket(SoundEvents.BEACON_ACTIVATE, 1f, 1f, x, y, z));
        ModPackets.CHANNEL.sendToPlayers(players, new RuneCirclePacket(tick, x, y, z));
    }

    public AABB getAABB() {
        return AABB.of(new BoundingBox(
                (int) startPos.x - 10, (int) startPos.y - 10, (int) startPos.z - 10,
                (int) startPos.x + 10, (int) startPos.y + 10, (int) startPos.z + 10
        ));
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Tick", tick);
        tag.putUUID("Player", playerUUID);
        tag.putIntArray("ObeliskPos", new int[]{ pos.getX(), pos.getY(), pos.getZ() });
        tag.putIntArray("TeleportPos", new int[]{ target.getX(), target.getY(), target.getZ() });
        ListTag list = new ListTag();
        list.add(DoubleTag.valueOf(startPos.x));
        list.add(DoubleTag.valueOf(startPos.y));
        list.add(DoubleTag.valueOf(startPos.z));
        tag.put("StartPos", list);

        return tag;
    }

    public static RuneCircle fromNbt(ServerLevel level, CompoundTag tag) {
        UUID player = tag.getUUID("Player");

        int[] ints = tag.getIntArray("ObeliskPos");
        BlockPos obeliskPos = new BlockPos(ints[0], ints[1], ints[2]);

        ints = tag.getIntArray("TeleportPos");
        BlockPos teleportPos = new BlockPos(ints[0], ints[1], ints[2]);

        ListTag startPosList = tag.getList("StartPos", 6);
        double x = startPosList.getDouble(0);
        double y = startPosList.getDouble(1);
        double z = startPosList.getDouble(2);

        return new RuneCircle(level, tag.getInt("Tick"), player, obeliskPos, teleportPos, x, y, z);
    }

    public void tick(ServerLevel level) {
        PlayerList list = level.getServer().getPlayerList();
        if (list.getPlayer(playerUUID) == null || list.getPlayer(playerUUID).isRemoved()) {
            if (idleTick++ >= 200) stopped = true;
            return;
        }
        ServerPlayer player = list.getPlayer(playerUUID);
        if (player.getX() != startPos.x || player.getY() != startPos.y || player.getZ() != startPos.z) {
            player.sendSystemMessage(Component.translatable("text.respawnobelisks.wormhole_closed"), true);
            List<ServerPlayer> players = level.getPlayers(p -> getAABB().contains(p.getX(), p.getY(), p.getZ()));
            ModPackets.CHANNEL.sendToPlayers(players, new PlayLocalSoundPacket(SoundEvents.BEACON_DEACTIVATE, 1f, 1f, startPos.x, startPos.y, startPos.z));
            ModPackets.CHANNEL.sendToPlayers(players, new RuneCirclePacket(80, startPos.x, startPos.y, startPos.z));
            stopped = true;
            if (level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity be)
                be.hasTeleportingEntity = false;
            return;
        }
        List<ServerPlayer> players = level.getPlayers(p -> getAABB().contains(p.getX(), p.getY(), p.getZ()));
        ModPackets.CHANNEL.sendToPlayers(players, new RuneCirclePacket(tick, startPos.x, startPos.y, startPos.z));
        if (tick++ >= maxTick) {
            stopped = true;
            execute(level, player);
        }
    }

    public void execute(ServerLevel level, ServerPlayer player) {
        RespawnObeliskBlockEntity blockEntity = (RespawnObeliskBlockEntity)level.getBlockEntity(pos);
        if (blockEntity == null) return;
        RespawnObeliskBlock block = (RespawnObeliskBlock) level.getBlockState(pos).getBlock();
        if (!player.getAbilities().instabuild) {
            ((LivingEntityAccessor) player).dropEverything(DamageSource.OUT_OF_WORLD);
            System.out.println("dropped?");
            player.setExperienceLevels(0);
            player.setExperiencePoints(0);
        }
        player.teleportTo(target.getX()+0.5, target.getY(), target.getZ()+0.5);
        blockEntity.restoreSavedItems(player);
        block.getRespawnLocation(true, true, TeleportConfig.forcedCurseOnTp, blockEntity.getBlockState(), pos, level, player);
        blockEntity.hasTeleportingEntity = false;
        blockEntity.syncWithClient();
    }

    @Override
    public String toString() {
        return "RuneCircle{" +
                "tick=" + tick +
                ", maxTick=" + maxTick +
                ", playerUUID=" + playerUUID +
                ", pos=" + pos +
                ", target=" + target +
                ", startPos=" + startPos +
                ", stopped=" + stopped +
                '}';
    }
}
