package com.redpxnda.respawnobelisks.registry.block;

import com.mojang.datafixers.util.Either;
import com.redpxnda.respawnobelisks.config.*;
import com.redpxnda.respawnobelisks.network.*;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.item.CoreItem;
import com.redpxnda.respawnobelisks.registry.particle.packs.ParticlePack;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.redpxnda.respawnobelisks.util.ObeliskUtils.getAABB;

public class RespawnObeliskBlock extends Block implements EntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<ParticlePack> PACK = EnumProperty.create("pack", ParticlePack.class);
    public static final DirectionProperty RESPAWN_SIDE = DirectionProperty.create("respawn_side");
    private static final VoxelShape HITBOX_BOTTOM_BASE = Block.box(1.5D, 1.0D, 1.5D, 14.5D, 32.0D, 14.5D);
    private static final VoxelShape HITBOX_BOTTOM_TRIM = Block.box(0D, 0D, 0D, 16D, 3D, 16D);
    private static final VoxelShape AABB_BOTTOM = Shapes.or(HITBOX_BOTTOM_BASE, HITBOX_BOTTOM_TRIM);
    private static final VoxelShape HITBOX_TOP_BASE = Block.box(1.5D, -15.0D, 1.5D, 14.5D, 16.0D, 14.5D);
    private static final VoxelShape HITBOX_TOP_TRIM = Block.box(0D, -16D, 0D, 16D, -13D, 16D);
    private static final VoxelShape AABB_TOP = Shapes.or(HITBOX_TOP_BASE, HITBOX_TOP_TRIM);
    public final Either<ResourceKey<Level>, String> obeliskHomeDimension;
    public final Supplier<Item> defaultCoreItem;
    public final TagKey<Item> coreTag;
    public final Supplier<String[]> obeliskChargeItems;

    public RespawnObeliskBlock(Properties pProperties, Either<ResourceKey<Level>, String> obeliskDimension, Supplier<Item> coreItem, TagKey<Item> coreTag, Supplier<String[]> obeliskChargeItems) {
        super(pProperties);
        this.obeliskHomeDimension = obeliskDimension;
        this.defaultCoreItem = coreItem;
        this.coreTag = coreTag;
        this.obeliskChargeItems = obeliskChargeItems;
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(RESPAWN_SIDE, Direction.NORTH).setValue(PACK, ParticlePack.DEFAULT));
    }

    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1.0F;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return pState.getValue(HALF) == DoubleBlockHalf.LOWER ? AABB_BOTTOM : AABB_TOP;
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        DoubleBlockHalf doubleblockhalf = pState.getValue(HALF);
        if (pFacing.getAxis() == Direction.Axis.Y && doubleblockhalf == DoubleBlockHalf.LOWER == (pFacing == Direction.UP)) {
            return pFacingState.is(this) && pFacingState.getValue(HALF) != doubleblockhalf ? pState : Blocks.AIR.defaultBlockState();
        } else {
            return doubleblockhalf == DoubleBlockHalf.LOWER && pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos blockpos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        if (blockpos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(blockpos.above()).canBeReplaced(pContext)) {
            return this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER);
        } else {
            return null;
        }
    }
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        pLevel.setBlock(pPos.above(), pState.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HALF, RESPAWN_SIDE, PACK);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return state.getValue(HALF).equals(DoubleBlockHalf.LOWER);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!ChargeConfig.perPlayerCharge && state.getValue(HALF).equals(DoubleBlockHalf.LOWER) && level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity)
            return (int) (blockEntity.getCharge((Player) null)*3f/20f);
        return 0;
    }

    public Optional<Vec3> getRespawnLocation(BlockState state, BlockPos pos, ServerLevel level, ServerPlayer player) {
        return getRespawnLocation(false, true, false, state, pos, level, player);
    }

    public Optional<Vec3> getRespawnLocation(boolean isTeleport, boolean shouldCost, boolean forceCurse, BlockState state, BlockPos pos, ServerLevel level, ServerPlayer player) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }
        if (level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity && (TrustedPlayersConfig.allowObeliskRespawning || blockEntity.isPlayerTrusted(player.getScoreboardName()))) {
            if (!CurseConfig.enableCurse) forceCurse = false;
            double charge = blockEntity.getCharge(player);
            MobEffectInstance MEI = null;
            if (player.hasEffect(ModRegistries.IMMORTALITY_CURSE.get()))
                MEI = player.getEffect(ModRegistries.IMMORTALITY_CURSE.get());
            boolean isMEI = MEI == null;
            if (shouldCost && (charge <= 0 || forceCurse) && CurseConfig.enableCurse) {
                int amplifier = isTeleport ?
                        (
                                player.hasEffect(ModRegistries.IMMORTALITY_CURSE.get()) ?
                                    Math.min(player.getEffect(ModRegistries.IMMORTALITY_CURSE.get()).getAmplifier()+CurseConfig.curseLevelIncrement, CurseConfig.curseMaxLevel-1) :
                                    CurseConfig.curseLevelIncrement-1
                        ) : CurseConfig.curseMaxLevel+1;
                if (!player.hasEffect(ModRegistries.IMMORTALITY_CURSE.get()) || isTeleport)
                    player.addEffect(
                            new MobEffectInstance(
                                    ModRegistries.IMMORTALITY_CURSE.get(),
                                    CurseConfig.curseDuration,
                                    amplifier
                            )
                    );
                if (forceCurse && charge > 0) {
                    ObeliskUtils.curseHandler(level, player, pos, state);
                }
            }
            boolean isNotFullyCursed = CurseConfig.enableCurse && (isMEI || MEI.getAmplifier() < CurseConfig.curseMaxLevel - 1);
            if (!CurseConfig.enableCurse || isNotFullyCursed || charge > 0) {
                if (charge > 0 && shouldCost && !forceCurse) player.removeEffect(ModRegistries.IMMORTALITY_CURSE.get());
                Optional<Block> blockHolder = Registry.BLOCK.getOptional(new ResourceLocation(ChargeConfig.infiniteChargeBlock));
                if (blockHolder.isPresent() && charge > 0 && shouldCost) {
                    Class<? extends Block> blockClass = blockHolder.get().getClass();
                    boolean isNegative = !blockClass.isInstance(level.getBlockState(pos.below()).getBlock());
                    if (isNegative) {
                        blockEntity.decreaseCharge(player, isTeleport ? TeleportConfig.teleportationChargeCost : ChargeConfig.obeliskDepleteAmount);
                        blockEntity.setLastRespawn(level.getGameTime());
                    } else {
                        blockEntity.setLastCharge(level.getGameTime());
                    }
                    List<ServerPlayer> players = level.getPlayers(p -> getAABB(player.getBlockX(), player.getBlockY(), player.getBlockZ()).contains(p.getX(), p.getY(), p.getZ()));
                    if (!players.contains(player)) players.add(player);
                    ModPackets.CHANNEL.sendToPlayers(players, new FirePackMethodPacket(isNegative ? "deplete" : "charge", player.getId(), state.getValue(PACK), pos));
                    if (isNegative) state.getValue(PACK).particleHandler.depleteServerHandler(level, player, pos);
                    else state.getValue(PACK).particleHandler.chargeServerHandler(level, player, pos);
                } else if (shouldCost && !forceCurse) {
                    if (CurseConfig.enableCurse) {
                        ObeliskUtils.curseHandler(level, player, pos, state);
                    }
                }
                BlockPos spawnPos = pos.relative(state.getValue(RESPAWN_SIDE));
                return Optional.of(new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5));
            }
        }
        return Optional.empty();
    }



    public InteractionResult use(BlockState state, Level pLevel, BlockPos pos, Player pPlayer, InteractionHand hand, BlockHitResult hitResult) {
        if (pLevel.isClientSide) {
            return InteractionResult.CONSUME;
        } else {
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                pos = pos.below();
                state = pLevel.getBlockState(pos);
            }
            if (pPlayer instanceof ServerPlayer player && pLevel instanceof ServerLevel level && level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity && (TrustedPlayersConfig.allowObeliskInteraction || blockEntity.isPlayerTrusted(player.getScoreboardName()))) {
                double charge = blockEntity.getCharge(player);
                // exploding if wrong dimension0
                if (obeliskHomeDimension.left().isPresent()) {
                    if (level.dimension() != obeliskHomeDimension.left().get()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
                        level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 3);
                        level.setBlock(pos.below(2), Blocks.AIR.defaultBlockState(), 3);
                        level.explode(null, DamageSource.badRespawnPointExplosion(), null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F, true, Explosion.BlockInteraction.DESTROY);
                        return InteractionResult.SUCCESS;
                    }
                }

                if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && !blockEntity.getItemStack().isEmpty())
                    return takeCore(player, blockEntity);
                if (blockEntity.getItemStack().isEmpty() && player.getMainHandItem().is(coreTag) && player.getMainHandItem().hasTag() && player.getMainHandItem().getOrCreateTag().contains("RespawnObeliskData"))
                    return placeCore(player, blockEntity);

                // Charging obelisk
                String[] chargeItems = obeliskChargeItems.get();
                if (chargeObelisk(chargeItems, charge, player, state, pos, blockEntity, level)) return InteractionResult.SUCCESS;

                Optional<Item> itemHolder = Registry.ITEM.getOptional(new ResourceLocation(ReviveConfig.revivalItem));
                if (
                        itemHolder.isPresent() &&
                        player.getMainHandItem().getItem() == itemHolder.get() &&
                        !player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem()) &&
                        !blockEntity.getItemNbt().isEmpty() &&
                        blockEntity.getItemNbt().contains("tag") &&
                        blockEntity.getItemNbt().getCompound("tag").contains("RespawnObeliskData")
                ) {
                    if (reviveEntities(itemHolder.get(), blockEntity, state, player, level, pos)) return InteractionResult.SUCCESS;
                } else if (blockEntity.getItemStack().isEmpty())
                    player.sendSystemMessage(Component.translatable("text.respawnobelisks.no_core"));
                else if (charge <= 0)
                    player.sendSystemMessage(Component.translatable("text.respawnobelisks.no_charge"));
                else { // Setting spawn point
                    int degrees = 90; // degrees for respawn
                    if (state.getValue(RESPAWN_SIDE) == Direction.NORTH) degrees = 180;
                    else if (state.getValue(RESPAWN_SIDE) == Direction.EAST) degrees = -90;
                    else if (state.getValue(RESPAWN_SIDE) == Direction.SOUTH) degrees = 0;
                    if ((player.getRespawnPosition() != null && !player.getRespawnPosition().equals(pos)) || player.getRespawnPosition() == null) {
                        List<ServerPlayer> players = level.getPlayers(p -> getAABB(player.getBlockX(), player.getBlockY(), player.getBlockZ()).contains(p.getX(), p.getY(), p.getZ()));
                        ModPackets.CHANNEL.sendToPlayers(players, new PlaySoundPacket(Registry.SOUND_EVENT.getOptional(new ResourceLocation(ChargeConfig.obeliskSetSpawnSound)).orElse(SoundEvents.UI_BUTTON_CLICK), 1f, 1f));
                    }
                    player.setRespawnPosition(level.dimension(), pos, degrees, false, true);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    public boolean chargeObelisk(String[] chargeItems, double charge, ServerPlayer player, BlockState state, BlockPos pos, RespawnObeliskBlockEntity blockEntity, ServerLevel level) {
        if (!CoreUtils.hasCapability(blockEntity.getItemStack(), CoreUtils.Capability.CHARGE)) return false;
        for (String str : chargeItems) {
            String[] sections = str.split("\\|"); // "minecraft:item|17|true" is str
            int chargeAmount = Integer.parseInt(sections[1]); // 17 ^
            boolean allowOverfill = sections.length >= 3 && Boolean.parseBoolean(sections[2]); // true ^
            Optional<Item> itemHolder = Registry.ITEM.getOptional(new ResourceLocation(sections[0]));
            boolean fullyCharged = charge == blockEntity.getMaxCharge() && chargeAmount > 0;
            boolean empty = charge == 0 && chargeAmount < 0;
            if (itemHolder.isPresent() &&
                    !player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem()) &&
                    player.getMainHandItem().getItem() == itemHolder.get() // if held item = item
                    && (charge + chargeAmount <= blockEntity.getMaxCharge() || allowOverfill) // if charge would go over 100, dont allow unless config says so
                    && (charge + chargeAmount >= 0 || allowOverfill) // if charge would go under 0, dont allow unless config says so
                    && !fullyCharged // if fully charged, don't allow more
                    && !empty // if empty charged, don't allow less
            ) {
                for (String str2 : chargeItems) {
                    Optional<Item> itemHolder2 = Registry.ITEM.getOptional(new ResourceLocation(str2.split("\\|")[0]));
                    itemHolder2.ifPresent(holder -> player.getCooldowns().addCooldown(holder, 30));
                }
                List<ServerPlayer> players = level.getPlayers(p -> getAABB(player.getBlockX(), player.getBlockY(), player.getBlockZ()).contains(p.getX(), p.getY(), p.getZ()));
                ModPackets.CHANNEL.sendToPlayers(players, new FirePackMethodPacket(chargeAmount < 0 ? "deplete" : "charge", player.getId(), state.getValue(PACK), pos));
                if (chargeAmount < 0) state.getValue(PACK).particleHandler.depleteServerHandler(level, player, pos);
                else state.getValue(PACK).particleHandler.chargeServerHandler(level, player, pos);
                blockEntity.increaseCharge(player, chargeAmount);
                if (chargeAmount < 0) blockEntity.setLastRespawn(level.getGameTime());
                else blockEntity.setLastCharge(level.getGameTime());
                if (!player.isCreative()) player.getMainHandItem().shrink(1);
                return true;
            }
        }
        return false;
    }

    public boolean reviveEntities(Item item, RespawnObeliskBlockEntity blockEntity, BlockState state, ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (!CoreUtils.hasCapability(blockEntity.getItemStack(), CoreUtils.Capability.REVIVE)) return false;
        if (!blockEntity.getItemNbt().getCompound("tag").getCompound("RespawnObeliskData").contains("SavedEntities"))
            blockEntity.getItemNbt().getCompound("tag").getCompound("RespawnObeliskData").put("SavedEntities", new ListTag());
        ListTag listTag = blockEntity.getItemNbt().getCompound("tag").getCompound("RespawnObeliskData").getList("SavedEntities", 10);

        if (!listTag.isEmpty()) {
            boolean hasFired = false;
            int count = 0;
            for (Tag tag : listTag) {
                if (count >= ReviveConfig.maxEntities) break;
                if (
                        tag instanceof CompoundTag compound &&
                                compound.contains("uuid") &&
                                compound.contains("type") &&
                                compound.contains("data")
                ) {
                    if (player.level instanceof ServerLevel serverLevel) {
                        Entity entity = serverLevel.getEntity(compound.getUUID("uuid"));
                        if ((entity == null || !entity.isAlive()) && blockEntity.getCharge(player) - ReviveConfig.revivalCost < 0) {
                            player.sendSystemMessage(Component.translatable("text.respawnobelisks.insufficient_charge"));
                            break;
                        }
                        if (entity != null && entity.isAlive())
                            continue;
                    }
                    Entity toSummon = Registry.ENTITY_TYPE.get(ResourceLocation.tryParse(compound.getString("type"))).create(player.level);
                    if (toSummon == null) continue;
                    toSummon.load(compound.getCompound("data"));
                    toSummon.setPos(pos.getX()+0.5, pos.getY()+2.5, pos.getZ()+0.5);
                    player.level.addFreshEntity(toSummon);
                    blockEntity.decreaseCharge(player, ReviveConfig.revivalCost);
                    hasFired = true;
                    count++;
                }
            }
            if (hasFired) {
                blockEntity.checkLimbo(true);
                ModPackets.CHANNEL.sendToPlayer(player, new PlayTotemAnimationPacket(item));
                if (!player.isCreative()) player.getMainHandItem().shrink(1);
                List<ServerPlayer> players = level.getPlayers(p -> getAABB(player.getBlockX(), player.getBlockY(), player.getBlockZ()).contains(p.getX(), p.getY(), p.getZ()));
                ModPackets.CHANNEL.sendToPlayers(players, new FirePackMethodPacket("totem", player.getId(), state.getValue(PACK), pos));
            }
        }
        return true;
    }

    public InteractionResult takeCore(ServerPlayer player, RespawnObeliskBlockEntity blockEntity) {
        player.setItemInHand(InteractionHand.MAIN_HAND, blockEntity.getItemStack());
        blockEntity.setItem(ItemStack.EMPTY);
        blockEntity.checkLimbo(false);
        blockEntity.updateObeliskName();
        blockEntity.syncWithClient();
        ModPackets.CHANNEL.sendToPlayer(player, new PlaySoundPacket(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0f, 1.0f));
        return InteractionResult.SUCCESS;
    }

    public InteractionResult placeCore(ServerPlayer player, RespawnObeliskBlockEntity blockEntity) {
        ItemStack toAdd = player.getMainHandItem().copy();
        toAdd.setCount(1);
        blockEntity.setItem(toAdd);
        blockEntity.checkLimbo(false);
        blockEntity.updateObeliskName();
        blockEntity.syncWithClient();
        player.getMainHandItem().shrink(1);
        ModPackets.CHANNEL.sendToPlayer(player, new PlaySoundPacket(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0f, 1.0f));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean isMoving) {
        if (blockState.is(newBlockState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RespawnObeliskBlockEntity be) {
            if (!be.getItemStack().isEmpty())
                Containers.dropItemStack(level, pos.getX()+0.5, pos.getY(), pos.getZ()+0.5, be.getItemStack());
            if (be.hasStoredItems)
                be.storedItems.values().forEach(inv -> inv.dropAll(level, pos.getX()+0.5, pos.getY(), pos.getZ()+0.5));
        }
        super.onRemove(blockState, level, pos, newBlockState, isMoving);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter getter, BlockPos pos) {
        if (state.getValue(HALF).equals(DoubleBlockHalf.UPPER))
            pos = pos.below();
        BlockEntity be = getter.getBlockEntity(pos);
        if (be instanceof RespawnObeliskBlockEntity blockEntity) {
            if (!TrustedPlayersConfig.allowObeliskBreaking && !blockEntity.isPlayerTrusted(player.getScoreboardName())) {
                if (player instanceof ServerPlayer serverPlayer) serverPlayer.sendSystemMessage(Component.translatable("text.respawnobelisks.untrusted"), true);
                return 0f;
            }
            if (!blockEntity.getItemStack().isEmpty() && !player.isShiftKeyDown()) {
                if (player instanceof ServerPlayer serverPlayer) serverPlayer.sendSystemMessage(Component.translatable("text.respawnobelisks.has_core"), true);
                return 0f;
            }
            if (blockEntity.hasStoredItems && !player.isShiftKeyDown()) {
                if (player instanceof ServerPlayer serverPlayer) serverPlayer.sendSystemMessage(Component.translatable("text.respawnobelisks.items_are_stored"), true);
                return 0f;
            }
            if (blockEntity.hasTeleportingEntity) {
                if (player instanceof ServerPlayer serverPlayer) serverPlayer.sendSystemMessage(Component.translatable("text.respawnobelisks.wormhole_open"), true);
                return 0f;
            }
        }
        return super.getDestroyProgress(state, player, getter, pos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        if (pState.getValue(HALF).equals(DoubleBlockHalf.UPPER)) return null;
        return ModRegistries.RESPAWN_OBELISK_BE.get().create(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModRegistries.RESPAWN_OBELISK_BE.get() ? (pLevel, pos, blockState, be) -> {
            if (be instanceof RespawnObeliskBlockEntity blockEntity)
                RespawnObeliskBlockEntity.tick(pLevel, pos, blockState, blockEntity);
        } : null;
    }
}
