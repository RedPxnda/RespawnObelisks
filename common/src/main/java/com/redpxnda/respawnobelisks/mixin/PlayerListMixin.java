package com.redpxnda.respawnobelisks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.redpxnda.respawnobelisks.config.DimensionsConfig;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(PlayerManager.class)
public abstract class PlayerListMixin {
    @Shadow @Final private MinecraftServer server;

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
    private Optional<Vec3d> RESPAWNOBELISKS_findRespawnPositionAndUseSpawnBlock(
            ServerWorld world, BlockPos bp, float orientation, boolean forced, boolean endPortalScreen, Operation<Optional<Vec3d>> original,
            ServerPlayerEntity player, @Local(ordinal = 0) LocalRef<ServerWorld> targetWorld, @Local(ordinal = 0) LocalRef<BlockPos> targetPos,
            @Local(ordinal = 0) LocalFloatRef targetAngle, @Local(ordinal = 0) LocalBooleanRef targetForced) {
        if (endPortalScreen) {
            if (RespawnObelisksConfig.INSTANCE.dimensions.endSpawnMode == DimensionsConfig.EndSpawnMode.WORLD_SPAWN) return Optional.empty();

            if (
                    RespawnObelisksConfig.INSTANCE.dimensions.endSpawnMode == DimensionsConfig.EndSpawnMode.WORLD_SPAWN_IF_IN_END &&
                    player.getSpawnPointDimension().equals(World.END)
            ) return Optional.empty();

            if (RespawnObelisksConfig.INSTANCE.dimensions.endSpawnMode == DimensionsConfig.EndSpawnMode.NON_END_SECONDARY && RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.enableSecondarySpawnPoints) {
                SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(player);
                if (facet != null) {
                    SpawnPoint point = facet.getLatestPoint();
                    if (point == null) return Optional.empty();
                    else {
                        for (SpawnPoint p : facet.points) {
                            if (!p.dimension().equals(World.END)) {
                                ServerWorld newWorld = server.getWorld(p.dimension());
                                targetWorld.set(newWorld);
                                targetPos.set(p.pos());
                                targetAngle.set(p.angle());
                                targetForced.set(p.forced());
                                return original.call(newWorld, p.pos(), p.angle(), p.forced(), true);
                            }
                        }
                        return Optional.empty();
                    }
                }
            }
        }
        return original.call(world, bp, orientation, forced, endPortalScreen);
    }
}
