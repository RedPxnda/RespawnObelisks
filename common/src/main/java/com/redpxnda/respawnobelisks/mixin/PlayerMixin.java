package com.redpxnda.respawnobelisks.mixin;

import com.google.common.collect.Iterables;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Optional;

@Mixin(PlayerEntity.class)
public class PlayerMixin {
    @Inject(method = "findRespawnPosition", at = @At("HEAD"), cancellable = true)
    private static void RESPAWNOBELISKS_redirectSpawnPosition(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive, CallbackInfoReturnable<Optional<Vec3d>> cir) {
        if (world.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity) {
            GlobalPos globalPos = GlobalPos.create(world.getRegistryKey(), pos);
            Collection<ServerPlayerEntity> players = blockEntity.respawningPlayers.get(globalPos);
            if (players.isEmpty()) return;
            ServerPlayerEntity player = Iterables.get(players, 0);
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof RespawnObeliskBlock rob) {
                players.remove(player);
                cir.setReturnValue(rob.getRespawnLocation(state, pos, world, player));
            }
        }
    }
}
