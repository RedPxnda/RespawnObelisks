package com.redpxnda.respawnobelisks.data.saved;

import com.redpxnda.respawnobelisks.mixin.LivingEntityAccessor;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.PlayLocalSoundPacket;
import com.redpxnda.respawnobelisks.network.RespawnAnchorInteractionPacket;
import com.redpxnda.respawnobelisks.network.RuneCirclePacket;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AnchorExplosion {
    private final int delay;
    private int tick;
    private final BlockPos pos;
    private final int charge;
    public boolean stopped = false;

    public AnchorExplosion(int tick, int delay, int charge, BlockPos pos) {
        this.tick = tick;
        this.delay = delay;
        this.pos = pos;
        this.charge = charge;
    }

    public Box getAABB() {
        return Box.from(new BlockBox(
                pos.getX() - 10, pos.getY() - 10, pos.getZ() - 10,
                pos.getX() + 10, pos.getY() + 10, pos.getZ() + 10
        ));
    }

    public NbtCompound save(NbtCompound tag) {
        tag.putInt("Tick", tick);
        tag.putInt("Delay", delay);
        tag.putInt("Charge", charge);
        tag.putIntArray("Pos", new int[]{ pos.getX(), pos.getY(), pos.getZ() });

        return tag;
    }

    public static AnchorExplosion fromNbt(NbtCompound tag) {
        int[] intArray = tag.getIntArray("Pos");
        BlockPos blockPos = new BlockPos(intArray[0], intArray[1], intArray[2]);

        return new AnchorExplosion(tag.getInt("Tick"), tag.getInt("Delay"), tag.getInt("Charge"), blockPos);
    }

    public void tick(ServerWorld level) {
        if (tick++ >= delay) {
            stopped = true;
            execute(level);
        } else {
            if (this.tick % 10 == 0) {
                List<ServerPlayerEntity> players = level.getPlayers(p -> getAABB().contains(p.getX(), p.getY(), p.getZ()));
                ModPackets.CHANNEL.sendToPlayers(players, new RespawnAnchorInteractionPacket(pos, false, charge));
            }
        }
    }

    public void execute(ServerWorld level) {
        level.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        List<ServerPlayerEntity> players = level.getPlayers(p -> getAABB().contains(p.getX(), p.getY(), p.getZ()));
        ModPackets.CHANNEL.sendToPlayers(players, new RespawnAnchorInteractionPacket(pos, true, charge));
        level.createExplosion(null, level.getDamageSources().badRespawnPoint(new Vec3d(pos.getX(), pos.getY(), pos.getZ())), null, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 5 + 2*charge, true, World.ExplosionSourceType.BLOCK);
    }

    @Override
    public String toString() {
        return "AnchorExplosion{" +
                "delay=" + delay +
                ", tick=" + tick +
                ", pos=" + pos +
                ", charge=" + charge +
                ", stopped=" + stopped +
                '}';
    }
}
