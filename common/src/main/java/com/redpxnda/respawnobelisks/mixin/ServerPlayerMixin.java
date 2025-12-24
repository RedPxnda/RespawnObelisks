package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.config.SecondarySpawnPointConfig;
import com.redpxnda.respawnobelisks.facet.FailedSpawnBlocks;
import com.redpxnda.respawnobelisks.facet.HardcoreRespawningTracker;
import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import com.redpxnda.respawnobelisks.network.AllowHardcoreRespawnPacket;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.registry.block.RadiantFlameBlock;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RadiantFlameBlockEntity;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Shadow public abstract ServerLevel serverLevel();

    @Shadow public abstract void sendSystemMessage(Component message);

    @Shadow private float respawnAngle;

    @Shadow private boolean respawnForced;

    @Shadow private ResourceKey<Level> respawnDimension;

    @Shadow private @Nullable BlockPos respawnPosition;

    @Inject(method = "getRespawnPosition", at = @At("RETURN"), cancellable = true)
    private void RESPAWNOBELISKS_getAndCacheSpawnPosition(CallbackInfoReturnable<BlockPos> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        boolean override = false;

        BlockPos bp = cir.getReturnValue();
        GlobalPos pos;

        if (RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.enableSecondarySpawnPoints) {
            SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(player);
            if (facet != null) {
                SpawnPoint point = facet.getValidSpawnPoint(player);
                if (point != null) {
                    respawnDimension = point.dimension();
                    respawnPosition = point.pos();
                    respawnAngle = point.angle();
                    respawnForced = point.forced();
                    pos = point.asGlobalPos();
                } else pos = null;
                override = true;
            } else pos = bp == null ? null : GlobalPos.of(player.getRespawnDimension(), bp);
        } else pos = bp == null ? null : GlobalPos.of(player.getRespawnDimension(), bp);

        if (pos != null) {
            BlockEntity blockEntity = player.getServer().getLevel(pos.dimension()).getBlockEntity(pos.pos());
            if (blockEntity instanceof RespawnObeliskBlockEntity robe) {
                robe.respawningPlayers.remove(pos, player);
                robe.respawningPlayers.put(pos, player);
            } else if (blockEntity instanceof RadiantFlameBlockEntity flame) { // redundant idc i fix later
                flame.respawningPlayers.remove(pos, player);
                flame.respawningPlayers.put(pos, player);
            }
        }

        if (override) cir.setReturnValue(pos == null ? null : pos.pos());
    }

    @Inject(method = "setRespawnPosition", at = @At("HEAD"), cancellable = true)
    private void RESPAWNOBELISKS_overrideSpawnSetting(ResourceKey<Level> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (!forced && pos != null) {
            Level world = serverLevel().getServer().getLevel(dimension);
            BlockState state = world == null ? null : world.getBlockState(pos);
            if (world != null && RespawnObelisksConfig.INSTANCE.behaviorOverrides.isBlockBanned(state)) {
                FailedSpawnBlocks facet = FailedSpawnBlocks.KEY.get(player);
                Block block = state.getBlock();
                if (facet != null && !facet.blocks.contains(block)) {
                    facet.blocks.add(block);
                    sendSystemMessage(Component.translatable("text.respawnobelisks.cannot_set_spawn").setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("text.respawnobelisks.cannot_set_spawn.hover")))));
                }
                ci.cancel();
                return;
            }
        }

        if (RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.enableSecondarySpawnPoints) {
            SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(player);
            if (facet == null) return;
            SpawnPoint point = new SpawnPoint(dimension, pos, angle, forced);

            if (pos == null) {
                facet.removeLatestPoint();
                return;
            }

            if (facet.points.contains(point)) {
                if (RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.enableBlockPriorities)
                    ci.cancel();
                else
                    facet.addPoint(point);
            } else if (facet.blockAdditionAllowed(player, serverLevel().getBlockState(pos).getBlock(), player.getServer())) {
                facet.addPoint(point);
                if (RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.enableBlockPriorities) facet.sortByPrio(player.getServer());
            } else if (!facet.points.contains(point)) {
                player.sendSystemMessage(Component.translatable("block.respawnobelisks.cannot_set_spawn"));
                ci.cancel();
            } else
                ci.cancel();
        }
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void RESPAWNOBELISKS_allowHardcoreRespawning(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        BlockPos pos = BlockPos.ZERO;
        if (RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.worldSpawnMode != SecondarySpawnPointConfig.PointSpawnMode.NEVER || RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.secondarySpawnMode != SecondarySpawnPointConfig.PointSpawnMode.NEVER) {
            pos = player.getRespawnPosition(); // updating player spawn points
            SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(player);
            if (facet != null) {
                SpawnPoint point = facet.getLatestPoint();

                facet.willRespawnAtWorldSpawn = false;
                facet.canChooseRespawn = RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.secondarySpawnMode.evaluate(point, player);
                facet.canChooseWorldSpawn = RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.worldSpawnMode.evaluate(point, player);

                facet.sendToClient(player);
            }
        }
        if (RespawnObelisksConfig.INSTANCE.allowHardcoreRespawning) {
            if (pos == BlockPos.ZERO) pos = player.getRespawnPosition();
            if (pos == null) return;
            ResourceKey<Level> dim = player.getRespawnDimension();
            ServerLevel world = player.getServer().getLevel(dim);
            if (world == null) return;
            BlockState state = world.getBlockState(pos);
            boolean canRespawn = (state.getBlock() instanceof RespawnObeliskBlock rob && rob.getRespawnLocation(false, false, false, state, pos, world, player).isPresent()) || (state.getBlock() instanceof RadiantFlameBlock flame && flame.getRespawnLocation(false, state, pos, world, player).isPresent());
            HardcoreRespawningTracker tracker = HardcoreRespawningTracker.KEY.get(player);
            if (tracker != null) tracker.canRespawn = canRespawn;
            ModPackets.CHANNEL.sendToPlayer(player, new AllowHardcoreRespawnPacket(canRespawn));
        }
    }
}
