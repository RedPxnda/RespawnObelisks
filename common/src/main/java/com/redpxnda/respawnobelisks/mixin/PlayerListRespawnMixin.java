package com.redpxnda.respawnobelisks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(PlayerList.class)
public class PlayerListRespawnMixin {
    @WrapOperation(
            method = "respawn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;moveTo(DDDFF)V")
    )
    private void RESPAWNOBELISKS_moveToMixin(ServerPlayer instance, double x, double y, double z, float yRot, float xRot, Operation<Void> original, ServerPlayer pPlayer) {
        BlockPos blockpos = pPlayer.getRespawnPosition();
        if (blockpos != null) {
            BlockState blockstate = pPlayer.getLevel().getBlockState(blockpos);
            if (blockstate.getBlock() instanceof RespawnObeliskBlock) {
                if (blockstate.getValue(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.NORTH) yRot = 180;
                else if (blockstate.getValue(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.EAST) yRot = -90;
                else if (blockstate.getValue(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.SOUTH) yRot = 0;
                else if (blockstate.getValue(RespawnObeliskBlock.RESPAWN_SIDE) == Direction.WEST) yRot = 90;
            }
        }
        instance.moveTo(x, y, z, yRot, xRot);
    }

    @WrapOperation(
            method = "respawn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;findRespawnPositionAndUseSpawnBlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;")
    )
    private Optional<Vec3> findRespawnPositionAndUseSpawnBlock(ServerLevel pServerLevel, BlockPos pSpawnBlockPos, float pPlayerOrientation, boolean pIsRespawnForced, boolean pRespawnAfterWinningTheGame, Operation<Optional<Vec3>> original, ServerPlayer pPlayer) {
        BlockState blockState = pServerLevel.getBlockState(pSpawnBlockPos);
        return blockState.getBlock() instanceof RespawnObeliskBlock block ?
                block.getRespawnLocation(blockState, pSpawnBlockPos, pServerLevel, pPlayer) :
                Player.findRespawnPositionAndUseSpawnBlock(pServerLevel, pSpawnBlockPos, pPlayerOrientation, pIsRespawnForced, pRespawnAfterWinningTheGame);
    }
}
