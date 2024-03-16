package com.redpxnda.respawnobelisks.registry.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FakeRespawnAnchorBlock extends RespawnAnchorBlock {
    public FakeRespawnAnchorBlock(Settings pProperties) {
        super(pProperties);
    }

    public ActionResult onUse(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockHitResult pHit) {
        return ActionResult.FAIL;
    }
}
