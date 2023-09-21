package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public interface DimensionValidator {
    boolean isValid(ServerLevel level, BlockState state, BlockPos pos, RespawnObeliskBlockEntity blockEntity, ServerPlayer player);
}