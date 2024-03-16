package com.redpxnda.respawnobelisks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redpxnda.respawnobelisks.config.DimensionsConfig;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Debug(export = true)
@Mixin(PlayerManager.class)
public abstract class PlayerListMixin {
    @WrapOperation(
            method = "respawnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;refreshPositionAndAngles(DDDFF)V")
    )
    private void RESPAWNOBELISKS_moveToMixin(ServerPlayerEntity instance, double x, double y, double z, float yRot, float xRot, Operation<Void> original, ServerPlayerEntity pPlayer) {
        BlockPos blockpos = pPlayer.getSpawnPointPosition();
        if (blockpos != null) {
            BlockState blockstate = pPlayer.getWorld().getBlockState(blockpos);
            if (blockstate.getBlock() instanceof RespawnObeliskBlock) {
                if (blockstate.get(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.NORTH) yRot = 180;
                else if (blockstate.get(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.EAST) yRot = -90;
                else if (blockstate.get(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.SOUTH) yRot = 0;
                else if (blockstate.get(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.WEST) yRot = 90;
            }
        }
        original.call(instance, x, y, z, yRot, xRot);
    }

    @WrapOperation(
            method = "respawnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;findRespawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;")
    )
    private Optional<Vec3d> RESPAWNOBELISKS_findRespawnPositionAndUseSpawnBlock(ServerWorld world, BlockPos bp, float orientation, boolean forced, boolean endPortalScreen, Operation<Optional<Vec3d>> original) {
        if (endPortalScreen) {
            if (RespawnObelisksConfig.INSTANCE.dimensions.endSpawnMode == DimensionsConfig.EndSpawnMode.WORLD_SPAWN)
                return Optional.empty();
        }
        return original.call(world, bp, orientation, forced, endPortalScreen);
    }
}
