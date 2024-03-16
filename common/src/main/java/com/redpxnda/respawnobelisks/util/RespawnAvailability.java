package com.redpxnda.respawnobelisks.util;

import com.redpxnda.nucleus.codec.tag.TaggableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public interface RespawnAvailability {
    Map<TaggableBlock, RespawnAvailability> availabilityProviders = new HashMap<>();

    static boolean canRespawnAt(SpawnPoint point, ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        BlockState state = world.getBlockState(point.pos());
        for (Map.Entry<TaggableBlock, RespawnAvailability> entry : availabilityProviders.entrySet()) {
            if (entry.getKey().matches(state.getBlock())) return entry.getValue().canRespawnAt(point, point.pos(), state, world, player);
        }
        return point.forced();
    }

    boolean canRespawnAt(SpawnPoint point, BlockPos pos, BlockState state, ServerWorld world, ServerPlayerEntity player);
}
