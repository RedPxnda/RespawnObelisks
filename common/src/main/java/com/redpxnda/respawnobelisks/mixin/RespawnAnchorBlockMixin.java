package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.respawnobelisks.data.saved.AnchorExplosions;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.FakeRespawnAnchorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RespawnAnchorBlock.class)
public abstract class RespawnAnchorBlockMixin {
    @Inject(
            method = "use",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void RESPAWNOBELISKS_useRespawnAnchor(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!((pPlayer.getMainHandItem().getItem().equals(Items.GLOWSTONE) || pPlayer.getOffhandItem().getItem().equals(Items.GLOWSTONE)) && pState.getValue(RespawnAnchorBlock.CHARGE) < 4)) {
            if (pLevel instanceof ServerLevel level) {
                int charge = pState.getValue(RespawnAnchorBlock.CHARGE);
                pLevel.setBlock(pPos, ModRegistries.fakeRespawnAnchor.get().defaultBlockState().setValue(RespawnAnchorBlock.CHARGE, charge), 3);
                AnchorExplosions.getCache(level).create(0, 60, charge, pPos);
                if (pPlayer instanceof ServerPlayer sp) ModRegistries.catalystCriterion.trigger(sp);
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}
