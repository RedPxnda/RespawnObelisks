package com.redpxnda.respawnobelisks.scheduled;

import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.RespawnAnchorInteractionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;

public class ScheduledRespawnAnchorTask {
    int tickDelay;
    int tick;
    ServerPlayer player;
    BlockPos pPos;
    public int charge;

    boolean executed = false;

    public ScheduledRespawnAnchorTask(int tickDelay, ServerPlayer player, BlockPos pPos, int charge) {
        this.tickDelay = tickDelay;
        this.tick = 0;
        this.player = player;
        this.pPos = pPos;
        this.charge = charge;
    }

    public final void tick() {
        if (this.tick++ >= this.tickDelay) {
            this.run();
            this.executed = true;
        } else {
            if (this.tick % 10 == 0)
                ModPackets.CHANNEL.sendToPlayer(player, new RespawnAnchorInteractionPacket(this.pPos, false, charge));
        }
    }

    public void run() {
        player.level.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
        ModPackets.CHANNEL.sendToPlayer(player, new RespawnAnchorInteractionPacket(this.pPos, true, charge));
        player.level.explode(null, DamageSource.badRespawnPointExplosion(), null, pPos.getX() + 0.5, pPos.getY(), pPos.getZ() + 0.5, 5 + 2*charge, true, Explosion.BlockInteraction.DESTROY);
    }

    public boolean hasBeenExecuted() {
        return this.executed;
    }
}
