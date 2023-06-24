package com.redpxnda.respawnobelisks.data.saved;

import com.redpxnda.respawnobelisks.mixin.LivingEntityAccessor;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.PlayLocalSoundPacket;
import com.redpxnda.respawnobelisks.network.RespawnAnchorInteractionPacket;
import com.redpxnda.respawnobelisks.network.RuneCirclePacket;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import java.util.List;

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

    public AABB getAABB() {
        return AABB.of(new BoundingBox(
                pos.getX() - 10, pos.getY() - 10, pos.getZ() - 10,
                pos.getX() + 10, pos.getY() + 10, pos.getZ() + 10
        ));
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Tick", tick);
        tag.putInt("Delay", delay);
        tag.putInt("Charge", charge);
        tag.putIntArray("Pos", new int[]{ pos.getX(), pos.getY(), pos.getZ() });

        return tag;
    }

    public static AnchorExplosion fromNbt(CompoundTag tag) {
        int[] intArray = tag.getIntArray("Pos");
        BlockPos blockPos = new BlockPos(intArray[0], intArray[1], intArray[2]);

        return new AnchorExplosion(tag.getInt("Tick"), tag.getInt("Delay"), tag.getInt("Charge"), blockPos);
    }

    public void tick(ServerLevel level) {
        if (tick++ >= delay) {
            stopped = true;
            execute(level);
        } else {
            if (this.tick % 10 == 0) {
                List<ServerPlayer> players = level.getPlayers(p -> getAABB().contains(p.getX(), p.getY(), p.getZ()));
                ModPackets.CHANNEL.sendToPlayers(players, new RespawnAnchorInteractionPacket(pos, false, charge));
            }
        }
    }

    public void execute(ServerLevel level) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        List<ServerPlayer> players = level.getPlayers(p -> getAABB().contains(p.getX(), p.getY(), p.getZ()));
        ModPackets.CHANNEL.sendToPlayers(players, new RespawnAnchorInteractionPacket(pos, true, charge));
        level.explode(null, DamageSource.badRespawnPointExplosion(), null, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 5 + 2*charge, true, Explosion.BlockInteraction.DESTROY);
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
