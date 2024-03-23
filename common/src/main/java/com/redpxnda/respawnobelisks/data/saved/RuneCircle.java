package com.redpxnda.respawnobelisks.data.saved;

import com.redpxnda.nucleus.util.PlayerUtil;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.mixin.LivingEntityAccessor;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.PlayLocalSoundPacket;
import com.redpxnda.respawnobelisks.network.RuneCirclePacket;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.UUID;

public class RuneCircle {
    public int tick;
    public int idleTick = 0;
    public final int maxTick = 100;
    public final UUID playerUUID;
    private final ItemStack item;
    public final BlockPos pos;
    public final BlockPos target;
    public final Vec3d startPos;
    public boolean stopped = false;

    public RuneCircle(ServerWorld level, int tick, UUID player, NbtCompound stack, BlockPos pos, BlockPos target, double x, double y, double z) {
        this.tick = tick;
        this.playerUUID = player;
        this.item = ItemStack.fromNbt(stack);
        this.pos = pos;
        RespawnObeliskBlockEntity blockEntity = (RespawnObeliskBlockEntity)level.getBlockEntity(pos);
        blockEntity.hasTeleportingEntity = true;
        blockEntity.syncWithClient();
        this.target = target;
        this.startPos = new Vec3d(x, y, z);
    }

    public RuneCircle(ServerWorld level, int tick, UUID player, ItemStack stack, BlockPos pos, BlockPos target, double x, double y, double z) {
        this.tick = tick;
        this.playerUUID = player;
        this.item = stack;
        this.pos = pos;
        RespawnObeliskBlockEntity blockEntity = (RespawnObeliskBlockEntity)level.getBlockEntity(pos);
        blockEntity.hasTeleportingEntity = true;
        blockEntity.syncWithClient();
        this.target = target;
        this.startPos = new Vec3d(x, y, z);
    }

    public RuneCircle(ServerWorld level, ServerPlayerEntity player, ItemStack stack, BlockPos pos, BlockPos target, double x, double y, double z) {
        this(level, 0, player.getUuid(), stack, pos, target, x, y, z);
        List<ServerPlayerEntity> players = level.getPlayers(p -> getAABB().contains(p.getX(), p.getY(), p.getZ()));
        ModPackets.CHANNEL.sendToPlayers(players, new PlayLocalSoundPacket(SoundEvents.BLOCK_BEACON_ACTIVATE, 1f, 1f, x, y, z));
        ModPackets.CHANNEL.sendToPlayers(players, new RuneCirclePacket(false, tick, x, y, z));
    }

    public Box getAABB() {
        return Box.from(new BlockBox(
                (int) startPos.x - 10, (int) startPos.y - 10, (int) startPos.z - 10,
                (int) startPos.x + 10, (int) startPos.y + 10, (int) startPos.z + 10
        ));
    }

    public NbtCompound save(NbtCompound tag) {
        tag.putInt("Tick", tick);
        tag.putUuid("Player", playerUUID);
        tag.put("Item", item.writeNbt(new NbtCompound()));
        tag.putIntArray("ObeliskPos", new int[]{ pos.getX(), pos.getY(), pos.getZ() });
        tag.putIntArray("TeleportPos", new int[]{ target.getX(), target.getY(), target.getZ() });
        NbtList list = new NbtList();
        list.add(NbtDouble.of(startPos.x));
        list.add(NbtDouble.of(startPos.y));
        list.add(NbtDouble.of(startPos.z));
        tag.put("StartPos", list);

        return tag;
    }

    public static RuneCircle fromNbt(ServerWorld level, NbtCompound tag) {
        UUID player = tag.getUuid("Player");

        int[] ints = tag.getIntArray("ObeliskPos");
        BlockPos obeliskPos = new BlockPos(ints[0], ints[1], ints[2]);

        ints = tag.getIntArray("TeleportPos");
        BlockPos teleportPos = new BlockPos(ints[0], ints[1], ints[2]);

        NbtList startPosList = tag.getList("StartPos", 6);
        double x = startPosList.getDouble(0);
        double y = startPosList.getDouble(1);
        double z = startPosList.getDouble(2);

        return new RuneCircle(level, tag.getInt("Tick"), player, tag.getCompound("Item"), obeliskPos, teleportPos, x, y, z);
    }

    public void tick(ServerWorld level) {
        PlayerManager list = level.getServer().getPlayerManager();
        if (list.getPlayer(playerUUID) == null || list.getPlayer(playerUUID).isRemoved()) {
            if (idleTick++ >= 200)
                stop(level);
            return;
        }
        ServerPlayerEntity player = list.getPlayer(playerUUID);
        if (
                player.getX() != startPos.x || player.getY() != startPos.y || player.getZ() != startPos.z ||
                PlayerUtil.getTotalXp(player) < RespawnObelisksConfig.INSTANCE.teleportation.xpCost || player.experienceLevel < RespawnObelisksConfig.INSTANCE.teleportation.levelCost ||
                player.getMainHandStack() != item
        ) {
            player.getItemCooldownManager().set(item.getItem(), RespawnObelisksConfig.INSTANCE.teleportation.teleportationBackupCooldown);
            player.sendMessageToClient(Text.translatable("text.respawnobelisks.wormhole_closed"), true);
            stop(level);
            return;
        }
        List<ServerPlayerEntity> players = level.getPlayers(p -> getAABB().contains(p.getX(), p.getY(), p.getZ()));
        ModPackets.CHANNEL.sendToPlayers(players, new RuneCirclePacket(false, tick, startPos.x, startPos.y, startPos.z));
        if (tick++ >= maxTick) {
            stopped = true;
            execute(level, player);
        }
    }

    private void stop(ServerWorld level) {
        List<ServerPlayerEntity> players = level.getPlayers(p -> getAABB().contains(p.getX(), p.getY(), p.getZ()));
        ModPackets.CHANNEL.sendToPlayers(players, new PlayLocalSoundPacket(SoundEvents.BLOCK_BEACON_DEACTIVATE, 1f, 1f, startPos.x, startPos.y, startPos.z));
        ModPackets.CHANNEL.sendToPlayers(players, new RuneCirclePacket(true, 80, startPos.x, startPos.y, startPos.z));
        stopped = true;
        if (level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity be)
            be.hasTeleportingEntity = false;
    }

    public void execute(ServerWorld level, ServerPlayerEntity player) {
        RespawnObeliskBlockEntity blockEntity = (RespawnObeliskBlockEntity)level.getBlockEntity(pos);
        if (blockEntity == null) return;
        RespawnObeliskBlock block = (RespawnObeliskBlock) level.getBlockState(pos).getBlock();
        player.getItemCooldownManager().set(player.getMainHandStack().getItem(), RespawnObelisksConfig.INSTANCE.teleportation.teleportationCooldown);
        if (RespawnObelisksConfig.INSTANCE.teleportation.xpCost > 0) player.addExperience(-RespawnObelisksConfig.INSTANCE.teleportation.xpCost);
        if (RespawnObelisksConfig.INSTANCE.teleportation.levelCost > 0) player.addExperienceLevels(-RespawnObelisksConfig.INSTANCE.teleportation.levelCost);
        if (RespawnObelisksConfig.INSTANCE.teleportation.dropCompassOnTp) {
            player.dropItem(player.getMainHandStack(), true, true);
            player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        if (!player.getAbilities().creativeMode && RespawnObelisksConfig.INSTANCE.teleportation.dropItemsOnTeleport) {
            ((LivingEntityAccessor) player).dropEverything(level.getDamageSources().generic());
            player.setExperienceLevel(0);
            player.setExperiencePoints(0);
        }
        player.requestTeleport(target.getX()+0.5, target.getY(), target.getZ()+0.5);
        ObeliskUtils.restoreSavedItems(player);
        block.getRespawnLocation(true, true, RespawnObelisksConfig.INSTANCE.teleportation.forcedCurseOnTp, blockEntity.getCachedState(), pos, level, player);
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
