package com.redpxnda.respawnobelisks.scheduled.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redpxnda.respawnobelisks.scheduled.Task;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public class ClientRuneCircleTask implements Task {
    private int tick = 0;
    private final int time;
    public final BlockPos pos;
    public final AABB aabb;
    private boolean executed = false;

    public ClientRuneCircleTask(int time, BlockPos pos) {
        this.time = time;
        this.pos = pos;
        this.aabb = AABB.of(new BoundingBox(
                pos.getX()-10, pos.getY()-10, pos.getZ()-10,
                pos.getX()+10, pos.getY()+10, pos.getZ()+10
        ));
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
