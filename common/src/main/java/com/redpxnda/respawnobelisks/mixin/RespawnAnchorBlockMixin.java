package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.saved.AnchorExplosions;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RespawnAnchorBlock.class)
public abstract class RespawnAnchorBlockMixin {
    @Inject(
            method = "onUse",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void RESPAWNOBELISKS_useRespawnAnchor(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockHitResult pHit, CallbackInfoReturnable<ActionResult> cir) {
        if (RespawnObelisksConfig.INSTANCE.behaviorOverrides.destructionCatalysts && !((pPlayer.getMainHandStack().getItem().equals(Items.GLOWSTONE) || pPlayer.getOffHandStack().getItem().equals(Items.GLOWSTONE)) && pState.get(RespawnAnchorBlock.CHARGES) < 4)) {
            if (pLevel instanceof ServerWorld level) {
                int charge = pState.get(RespawnAnchorBlock.CHARGES);
                pLevel.setBlockState(pPos, ModRegistries.fakeRespawnAnchor.get().getDefaultState().with(RespawnAnchorBlock.CHARGES, charge), 3);
                AnchorExplosions.getCache(level).create(0, 60, charge, pPos);
                if (pPlayer instanceof ServerPlayerEntity sp) ModRegistries.catalystCriterion.trigger(sp);
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }
}
