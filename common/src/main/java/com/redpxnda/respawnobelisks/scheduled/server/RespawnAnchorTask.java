package com.redpxnda.respawnobelisks.scheduled.server;

import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.RespawnAnchorInteractionPacket;
import com.redpxnda.respawnobelisks.scheduled.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RespawnAnchorTask implements Task {
    private final int tickDelay;
    private int tick;
    private final ServerLevel level;
    private final BlockPos pos;
    public final int charge;

    private boolean executed = false;

    public RespawnAnchorTask(int tickDelay, ServerLevel level, BlockPos pos, int charge) {
        this.tickDelay = tickDelay;
        this.tick = 0;
        this.level = level;
        this.pos = pos;
        this.charge = charge;
    }

    public void tick() {
        if (this.tick++ >= this.tickDelay) {
            this.fire();
            this.executed = true;
        } else {
            if (this.tick % 10 == 0) {
                AABB aabb = AABB.of(new BoundingBox(
                        pos.getX()-10, pos.getY()-10, pos.getZ()-10,
                        pos.getX()+10, pos.getY()+10, pos.getZ()+10
                ));
                List<ServerPlayer> players = level.getPlayers(p -> aabb.contains(p.getX(), p.getY(), p.getZ()));
                ModPackets.CHANNEL.sendToPlayers(players, new RespawnAnchorInteractionPacket(this.pos, false, charge));
            }
        }
    }

    public void fire() {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        AABB aabb = AABB.of(new BoundingBox(
                pos.getX()-10, pos.getY()-10, pos.getZ()-10,
                pos.getX()+10, pos.getY()+10, pos.getZ()+10
        ));
        List<ServerPlayer> players = level.getPlayers(p -> aabb.contains(p.getX(), p.getY(), p.getZ()));
        ModPackets.CHANNEL.sendToPlayers(players, new RespawnAnchorInteractionPacket(this.pos, true, charge));
        level.explode(null, DamageSource.badRespawnPointExplosion(), null, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 5 + 2*charge, true, Explosion.BlockInteraction.DESTROY);
    }

    @Override
    public boolean hasBeenExecuted() {
        return this.executed;
    }
}
