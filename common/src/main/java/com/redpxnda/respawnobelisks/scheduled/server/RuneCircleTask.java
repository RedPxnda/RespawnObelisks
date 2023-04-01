package com.redpxnda.respawnobelisks.scheduled.server;

import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.RuneCirclePacket;
import com.redpxnda.respawnobelisks.scheduled.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RuneCircleTask implements Task {
    private int tick = 0;
    private final int time;
    private final BlockPos pos;
    private final ServerLevel level;
    private boolean executed = false;

    public RuneCircleTask(int time, BlockPos pos, ServerLevel level) {
        this.time = time;
        this.pos = pos;
        this.level = level;
        AABB aabb = AABB.of(new BoundingBox(
                pos.getX()-10, pos.getY()-10, pos.getZ()-10,
                pos.getX()+10, pos.getY()+10, pos.getZ()+10
        ));
        List<ServerPlayer> players = level.getPlayers(p -> aabb.contains(p.getX(), p.getY(), p.getZ()));
        ModPackets.CHANNEL.sendToPlayers(players, new RuneCirclePacket(time, pos));
    }


    @Override
    public void tick() {
        if (this.tick++ >= this.time) {
            this.fire();
            this.executed = true;
        }
    }

    @Override
    public void fire() {}

    @Override
    public boolean hasBeenExecuted() {
        return executed;
    }
}
