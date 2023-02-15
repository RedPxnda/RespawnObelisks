package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.respawnobelisks.registry.Registry;
import com.redpxnda.respawnobelisks.scheduled.ScheduledRespawnAnchorTask;
import com.redpxnda.respawnobelisks.scheduled.ScheduledTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RespawnAnchorBlock.class)
public class RespawnAnchorBlockMixin {
    @Inject(
            method = "use",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!((pPlayer.getMainHandItem().getItem().equals(Items.GLOWSTONE) || pPlayer.getOffhandItem().getItem().equals(Items.GLOWSTONE)) && pState.getValue(RespawnAnchorBlock.CHARGE) < 4)) {
            if (pPlayer instanceof ServerPlayer player) {
                int charge = pState.getValue(RespawnAnchorBlock.CHARGE);
                pLevel.setBlock(pPos, Registry.FAKE_ANCHOR_BLOCK.get().defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, charge), 3);
                ScheduledRespawnAnchorTask newTask = new ScheduledRespawnAnchorTask(60, player, pPos, charge);
                ScheduledTasks.schedule(newTask);
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}
