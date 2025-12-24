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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Shadow @Final private MinecraftServer server;

    @WrapOperation(
            method = "respawn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;moveTo(DDDFF)V")
    )
    private void RESPAWNOBELISKS_moveToMixin(ServerPlayer instance, double x, double y, double z, float yRot, float xRot, Operation<Void> original, ServerPlayer pPlayer) {
        BlockPos blockpos = pPlayer.getRespawnPosition();
        if (blockpos != null) {
            BlockState blockstate = pPlayer.level().getBlockState(blockpos);
            if (blockstate.getBlock() instanceof RespawnObeliskBlock) {
                if (blockstate.getValue(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.NORTH) yRot = 180;
                else if (blockstate.getValue(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.EAST) yRot = -90;
                else if (blockstate.getValue(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.SOUTH) yRot = 0;
                else if (blockstate.getValue(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.WEST) yRot = 90;
            }
        }
        original.call(instance, x, y, z, yRot, xRot);
    }

    @WrapOperation(
            method = "respawn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;findRespawnPositionAndUseSpawnBlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;")
    )
    private Optional<Vec3> RESPAWNOBELISKS_findRespawnPositionAndUseSpawnBlock(
            ServerLevel world, BlockPos bp, float orientation, boolean forced, boolean endPortalScreen, Operation<Optional<Vec3>> original,
            ServerPlayer player, @Local(ordinal = 0) LocalRef<ServerLevel> targetWorld, @Local(ordinal = 0) LocalRef<BlockPos> targetPos,
            @Local(ordinal = 0) LocalFloatRef targetAngle, @Local(ordinal = 0) LocalBooleanRef targetForced) {
        if (endPortalScreen) {
            if (RespawnObelisksConfig.INSTANCE.dimensions.endSpawnMode == DimensionsConfig.EndSpawnMode.WORLD_SPAWN) return Optional.empty();

            if (
                    RespawnObelisksConfig.INSTANCE.dimensions.endSpawnMode == DimensionsConfig.EndSpawnMode.WORLD_SPAWN_IF_IN_END &&
                    player.getRespawnDimension().equals(Level.END)
            ) return Optional.empty();

            if (RespawnObelisksConfig.INSTANCE.dimensions.endSpawnMode == DimensionsConfig.EndSpawnMode.NON_END_SECONDARY && RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.enableSecondarySpawnPoints) {
                SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(player);
                if (facet != null) {
                    SpawnPoint point = facet.getLatestPoint();
                    if (point == null) return Optional.empty();
                    else {
                        for (SpawnPoint p : facet.points) {
                            if (!p.dimension().equals(Level.END)) {
                                ServerLevel newWorld = server.getLevel(p.dimension());
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
