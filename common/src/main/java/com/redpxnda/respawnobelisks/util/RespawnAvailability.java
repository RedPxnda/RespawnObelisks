package com.redpxnda.respawnobelisks.util;

import com.redpxnda.nucleus.codec.tag.TaggableBlock;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public interface RespawnAvailability {
    Map<TaggableBlock, RespawnAvailability> availabilityProviders = new HashMap<>();

    static boolean canRespawnAt(SpawnPoint point, ServerPlayer player) {
        ServerLevel world = player.getServer().getLevel(point.dimension());
        BlockState state = world.getBlockState(point.pos());
        for (Map.Entry<TaggableBlock, RespawnAvailability> entry : availabilityProviders.entrySet()) {
            if (entry.getKey().matches(state.getBlock())) return entry.getValue().canRespawnAt(point, point.pos(), state, world, player);
        }
        return Player.findRespawnPositionAndUseSpawnBlock(world, point.pos(), point.angle(), point.forced(), player.isAlive()).isPresent();
    }

    boolean canRespawnAt(SpawnPoint point, BlockPos pos, BlockState state, ServerLevel world, ServerPlayer player);
}
