package com.redpxnda.respawnobelisks.registry.block;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RadiantFlameBlockEntity;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RadiantFlameBlock extends Block implements EntityBlock { // todo prevent sprint particles - Entity#spawnSprintParticle
    private static final VoxelShape HITBOX = Block.box(1.5D, 1.0D, 1.5D, 14.5D, 16.0D, 14.5D);

    public RadiantFlameBlock(Properties settings) {
        super(settings);
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof RadiantFlameBlockEntity blockEntity) {
            blockEntity.setCharge(CoreUtils.getCharge(stack.getOrCreateTag()));
            blockEntity.setInitialCharge(blockEntity.getCharge());
            blockEntity.setOwner(placer.getUUID());
        }

        if (placer instanceof ServerPlayer player) {
            Vec3 centerPos = pos.getCenter();
            Vec3 playerPos = player.position();
            float angle = (float) Math.atan2(playerPos.z - centerPos.z, playerPos.x - centerPos.x); // finding spawn angle based on where player is standing
            player.setRespawnPosition(level.dimension(), pos, angle, false, true);
        }
    }

    public Optional<Vec3> getRespawnLocation(boolean shouldCost, BlockState state, BlockPos pos, ServerLevel level, ServerPlayer player) {
        if ( // condition stuff
                level.getBlockEntity(pos) instanceof RadiantFlameBlockEntity blockEntity &&
                (!RespawnObelisksConfig.INSTANCE.radiantFlame.playerBound || blockEntity.getOwner() == null || blockEntity.getOwner().equals(player.getUUID()))
        ) {
            double charge = blockEntity.getCharge();
            double cost = RespawnObelisksConfig.INSTANCE.radiance.respawnCost; // preparing cost value

            //if (charge-cost >= 0 && shouldCost) player.removeStatusEffect(immortalityCurse.get()); // remove curse if charge

            if (charge - (RespawnObelisksConfig.INSTANCE.radiance.forgivingRespawn ? 0 : cost) <= 0) {
                if (shouldCost) blockEntity.remove();
                return Optional.empty();
            }

            if (shouldCost)
                blockEntity.decreaseCharge(cost);

            Vec3 centered = pos.getCenter();
            return Optional.of(centered);
        }
        return Optional.empty();
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            Vec3 centerPos = pos.getCenter();
            Vec3 playerPos = player.position();
            float angle = (float) Math.atan2(playerPos.z - centerPos.z, playerPos.x - centerPos.x); // finding spawn angle based on where player is standing
            serverPlayer.setRespawnPosition(level.dimension(), pos, angle, false, true);
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hitResult);
    }

    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
        return HITBOX;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return ModRegistries.radiantFlameBlockEntity.get().create(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModRegistries.radiantFlameBlockEntity.get() ? (pLevel, pos, blockState, be) -> {
            if (be instanceof RadiantFlameBlockEntity blockEntity)
                blockEntity.tick(pLevel, pos, blockState);
        } : null;
    }
}
