package com.redpxnda.respawnobelisks.mixin;

import com.google.common.collect.Iterables;
import com.redpxnda.respawnobelisks.registry.block.RadiantFlameBlock;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RadiantFlameBlockEntity;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Optional;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("HEAD"), cancellable = true)
    private static void RESPAWNOBELISKS_redirectSpawnPosition(ServerLevel world, BlockPos pos, float angle, boolean forced, boolean alive, CallbackInfoReturnable<Optional<Vec3>> cir) {
        if (world.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity) {
            GlobalPos globalPos = GlobalPos.of(world.dimension(), pos);
            Collection<ServerPlayer> players = blockEntity.respawningPlayers.get(globalPos);
            if (players.isEmpty()) return;
            ServerPlayer player = Iterables.get(players, 0);
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof RespawnObeliskBlock rob) {
                players.remove(player);
                cir.setReturnValue(rob.getRespawnLocation(state, pos, world, player));
            }
        } else if (world.getBlockEntity(pos) instanceof RadiantFlameBlockEntity blockEntity) { // yeah yeah ik this is lazy ash but i do not give a shit
            GlobalPos globalPos = GlobalPos.of(world.dimension(), pos);
            Collection<ServerPlayer> players = blockEntity.respawningPlayers.get(globalPos);
            if (players.isEmpty()) return;
            ServerPlayer player = Iterables.get(players, 0);
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof RadiantFlameBlock b) {
                players.remove(player);
                cir.setReturnValue(b.getRespawnLocation(true, state, pos, world, player));
            }
        }
    }
}
