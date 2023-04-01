package com.redpxnda.respawnobelisks.registry.item;

import com.mojang.logging.LogUtils;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.scheduled.server.RuneCircleTask;
import com.redpxnda.respawnobelisks.scheduled.server.ScheduledServerTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.Optional;

public class BoundCompassItem extends CompassItem {
    private static final Logger LOGGER = LogUtils.getLogger();

    public BoundCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        if (useOnContext.getPlayer() == null) return super.useOn(useOnContext);
        Player player = useOnContext.getPlayer();
        BlockPos blockPos = useOnContext.getClickedPos();
        Level level = useOnContext.getLevel();
        if (level.getBlockState(blockPos).is(Blocks.LODESTONE)) {
            level.playSound(null, blockPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
            ItemStack itemStack = useOnContext.getItemInHand();
            if (!player.getAbilities().instabuild && itemStack.getCount() == 1) {
                this.addLodestoneTags(level.dimension(), blockPos, itemStack.getOrCreateTag());
            } else {
                ItemStack itemStack2 = new ItemStack(ModRegistries.BOUND_COMPASS.get(), 1);
                CompoundTag compoundTag = itemStack.hasTag() ? itemStack.getTag().copy() : new CompoundTag();
                itemStack2.setTag(compoundTag);
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                this.addLodestoneTags(level.dimension(), blockPos, compoundTag);
                if (!player.getInventory().add(itemStack2)) {
                    player.drop(itemStack2, false);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else if (useOnContext.getHand().equals(InteractionHand.MAIN_HAND) && player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
            GlobalPos pos = getLodestonePosition(player.getMainHandItem().getOrCreateTag());
            if (pos != null && level.getBlockState(pos.pos().above()).getBlock() instanceof RespawnObeliskBlock block && level.getBlockEntity(pos.pos().above()) instanceof RespawnObeliskBlockEntity blockEntity) {
                BlockState state = level.getBlockState(pos.pos().above());
                Optional<Vec3> obeliskLoc = block.getRespawnLocation(true, state, pos.pos().above(), serverLevel, serverPlayer);
                obeliskLoc.ifPresent(vec3 -> {
                    player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), 100);
                    //player.teleportTo(vec3.x, vec3.y, vec3.z);
                    ScheduledServerTasks.schedule(new RuneCircleTask(100, new BlockPos(player.getX(), player.getY(), player.getZ()), serverLevel));
                });
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(useOnContext);
    }



    private void addLodestoneTags(ResourceKey<Level> resourceKey, BlockPos blockPos, CompoundTag compoundTag) {
        compoundTag.put(TAG_LODESTONE_POS, NbtUtils.writeBlockPos(blockPos));
        Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, resourceKey).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put(TAG_LODESTONE_DIMENSION, (Tag)tag));
        compoundTag.putBoolean(TAG_LODESTONE_TRACKED, true);
    }
}