package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface DimensionValidator {
    boolean isValid(ServerWorld level, BlockState state, BlockPos pos, RespawnObeliskBlockEntity blockEntity, ServerPlayerEntity player);
}