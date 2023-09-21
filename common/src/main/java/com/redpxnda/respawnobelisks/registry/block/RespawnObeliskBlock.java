package com.redpxnda.respawnobelisks.registry.block;

import com.redpxnda.respawnobelisks.config.*;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.ParticleAnimationPacket;
import com.redpxnda.respawnobelisks.network.PlaySoundPacket;
import com.redpxnda.respawnobelisks.network.PlayTotemAnimationPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.DimensionValidator;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.redpxnda.respawnobelisks.registry.ModRegistries.immortalityCurse;
import static com.redpxnda.respawnobelisks.util.ObeliskUtils.getAABB;

public class RespawnObeliskBlock extends Block implements EntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty WILD = BooleanProperty.create("wild");
    public static final DirectionProperty RESPAWN_SIDE = DirectionProperty.create("respawn_side");
    private static final VoxelShape HITBOX_BOTTOM_BASE = Block.box(1.5D, 1.0D, 1.5D, 14.5D, 32.0D, 14.5D);
    private static final VoxelShape HITBOX_BOTTOM_TRIM = Block.box(0D, 0D, 0D, 16D, 3D, 16D);
    private static final VoxelShape AABB_BOTTOM = Shapes.or(HITBOX_BOTTOM_BASE, HITBOX_BOTTOM_TRIM);
    private static final VoxelShape HITBOX_TOP_BASE = Block.box(1.5D, -15.0D, 1.5D, 14.5D, 16.0D, 14.5D);
    private static final VoxelShape HITBOX_TOP_TRIM = Block.box(0D, -16D, 0D, 16D, -13D, 16D);
    private static final VoxelShape AABB_TOP = Shapes.or(HITBOX_TOP_BASE, HITBOX_TOP_TRIM);
    public final @Nullable DimensionValidator dimension;

    public RespawnObeliskBlock(Properties pProperties, @Nullable DimensionValidator obeliskDimension) {
        super(pProperties);
        this.dimension = obeliskDimension;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(RESPAWN_SIDE, Direction.NORTH)
                .setValue(WILD, false)
        );
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
        pBuilder.add(HALF, RESPAWN_SIDE, WILD);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return state.getValue(HALF).equals(DoubleBlockHalf.LOWER);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (/*!ChargeConfig.perPlayerCharge && */state.getValue(HALF).equals(DoubleBlockHalf.LOWER) && level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity)
            return (int) (blockEntity.getCharge(null)*3f/20f);
        return 0;
    }

    public Optional<Vec3> getRespawnLocation(BlockState state, BlockPos pos, ServerLevel level, ServerPlayer player) {
        return getRespawnLocation(false, true, false, state, pos, level, player);
    }

    /**
     * @param isTeleport Whether this call is called by teleportation (recovery compass)
     * @param shouldCost Whether this call should actually take charge
     * @param forceCurse Whether the immortality curse should be automatically forced (usually used in tandem with isTeleport)
     * @return Calculated respawn position. Will be an empty {@link Optional} if player should be sent to spawn.
     */
    public Optional<Vec3> getRespawnLocation(boolean isTeleport, boolean shouldCost, boolean forceCurse, BlockState state, BlockPos pos, ServerLevel level, ServerPlayer player) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }
        if ( // condition stuff
                level.getBlockEntity(pos) != null &&
                level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity && // make sure block entity is found
                !blockEntity.getCoreInstance().isEmpty() &&
                (TrustedPlayersConfig.allowObeliskRespawning || blockEntity.isPlayerTrusted(player.getScoreboardName()))
        ) {
            if (!CurseConfig.enableCurse) forceCurse = false; // override curse coercion

            double charge = blockEntity.getCharge(player);
            double cost = !shouldCost ? 0 : isTeleport ? TeleportConfig.teleportationChargeCost : ChargeConfig.obeliskDepleteAmount; // preparing cost value

            for (ObeliskInteraction i : ObeliskInteraction.RESPAWN_INTERACTIONS.get(ObeliskInteraction.Injection.START)) { // Obelisk Respawn Interactions, for the start injection point
                ObeliskInteraction.Manager manager = new ObeliskInteraction.Manager(cost, null);
                i.respawnHandler.accept(player, blockEntity, manager);
                cost = manager.cost;
            }

            if (charge-cost >= 0 && shouldCost && !forceCurse) player.removeEffect(immortalityCurse.get()); // remove curse if charge

            MobEffectInstance mei = null;
            if (player.hasEffect(immortalityCurse.get()) && (mei = player.getEffect(immortalityCurse.get())).getAmplifier() >= CurseConfig.curseMaxLevel-1)
                return Optional.empty(); // if curse level is over the max, send to spawn

            boolean hasPlayedCurseAnim = false;
            if (CurseConfig.enableCurse && (charge-cost < 0 || forceCurse) && shouldCost) { // if cost brings charge below 0, curse
                int amplifier = isTeleport ?
                        (
                                mei != null ?
                                        Math.min(mei.getAmplifier()+CurseConfig.curseLevelIncrement, CurseConfig.curseMaxLevel-1) :
                                        CurseConfig.curseLevelIncrement-1
                        ) : CurseConfig.curseMaxLevel+1; // may seem odd to do 1 more than the curse max level, but see the clone handler in CommonEvents to understand
                if (!player.hasEffect(ModRegistries.immortalityCurse.get()) || isTeleport) // I would add another check for 'forceCurse' here, but it can cause issues if this method is used incorrectly.
                    player.addEffect(
                            new MobEffectInstance(
                                    ModRegistries.immortalityCurse.get(),
                                    CurseConfig.curseDuration,
                                    amplifier
                            )
                    );
                ObeliskUtils.curseHandler(level, player, pos, state); // curse animation and whatnot
                hasPlayedCurseAnim = true;
            }

            if (shouldCost && !hasPlayedCurseAnim)
                blockEntity.chargeAndAnimate(player, -cost); // actually charge
            else if (shouldCost)
                blockEntity.decreaseCharge(player, cost); // unfortunately, skipping the animation stuff means the lastRespawn value doesn't get updated during curse respawns

            BlockPos spawnPos = pos.relative(state.getValue(RESPAWN_SIDE));
            Vec3 vec = new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

            for (ObeliskInteraction i : ObeliskInteraction.RESPAWN_INTERACTIONS.get(ObeliskInteraction.Injection.END)) { // Obelisk Respawn Interactions, for the end injection point
                ObeliskInteraction.Manager manager = new ObeliskInteraction.Manager(cost, vec);
                i.respawnHandler.accept(player, blockEntity, manager);
                vec = manager.getSpawnLocActual();
            }

            ModRegistries.respawnCriterion.trigger(player); // achievement bs
            return Optional.of(vec);
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
                if (pPlayer.getMainHandItem().is(Items.BEDROCK) && pPlayer.getOffhandItem().is(Items.TALL_GRASS)) { // for wild obelisk setup
                    pLevel.setBlock(pos, state.setValue(WILD, true), 3);
                    blockEntity.hasRandomCharge = true;
                    return InteractionResult.SUCCESS;
                }

                double charge = blockEntity.getCharge(player);
                // exploding if wrong dimension
                if (dimension != null && !dimension.isValid(level, state, pos, blockEntity, player)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
                    level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 3);
                    level.explode(null, level.damageSources().badRespawnPointExplosion(pos.getCenter()), null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F, true, Level.ExplosionInteraction.BLOCK);
                    ModRegistries.kaboomCriterion.trigger(player);
                    return InteractionResult.SUCCESS;
                }

                if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && !blockEntity.getItemStack().isEmpty())
                    return takeCore(player, blockEntity);
                ResourceLocation rl;
                if (blockEntity.getItemStack().isEmpty() && ObeliskCore.CORES.containsKey(rl = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem())))
                    return placeCore(player, blockEntity, rl);

                // right click interactions
                if (clickInteractions(player, blockEntity)) return InteractionResult.SUCCESS;

                Optional<Item> itemHolder = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(ReviveConfig.revivalItem)); // to-do: make into interaction
                if (
                        CoreUtils.hasInteraction(blockEntity.getCoreInstance(), ObeliskInteraction.REVIVE) &&
                        itemHolder.isPresent() &&
                        player.getMainHandItem().getItem() == itemHolder.get() &&
                        !player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem()) &&
                        !blockEntity.getItemNbt().isEmpty() &&
                        blockEntity.getItemNbt().contains("tag") &&
                        blockEntity.getItemNbt().getCompound("tag").contains("RespawnObeliskData")
                ) {
                    if (reviveEntities(itemHolder.get(), blockEntity, player, level, pos)) return InteractionResult.SUCCESS;
                } else if (blockEntity.getCoreInstance().isEmpty())
                    player.sendSystemMessage(Component.translatable("text.respawnobelisks.no_core"));
                else if (charge <= 0 && !ChargeConfig.allowEmptySpawnSetting)
                    player.sendSystemMessage(Component.translatable("text.respawnobelisks.no_charge"));
                else { // Setting spawn point
                    int degrees = 90; // degrees for respawn
                    if (state.getValue(RESPAWN_SIDE) == Direction.NORTH) degrees = 180;
                    else if (state.getValue(RESPAWN_SIDE) == Direction.EAST) degrees = -90;
                    else if (state.getValue(RESPAWN_SIDE) == Direction.SOUTH) degrees = 0;
                    if ((player.getRespawnPosition() != null && !player.getRespawnPosition().equals(pos)) || player.getRespawnPosition() == null) {
                        List<ServerPlayer> players = level.getPlayers(p -> getAABB(blockEntity.getBlockPos()).contains(p.getX(), p.getY(), p.getZ()));
                        ModPackets.CHANNEL.sendToPlayers(players, new PlaySoundPacket(BuiltInRegistries.SOUND_EVENT.getOptional(new ResourceLocation(ChargeConfig.obeliskSetSpawnSound)).orElse(SoundEvents.UI_BUTTON_CLICK.value()), 1f, 1f));
                    }
                    player.setRespawnPosition(level.dimension(), pos, degrees, false, true);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    public boolean clickInteractions(Player player, RespawnObeliskBlockEntity blockEntity) {
        if (player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())) return false;
        ObeliskCore.Instance core = blockEntity.getCoreInstance();
        boolean returnValue = false;
        for (ObeliskInteraction interaction : ObeliskInteraction.RIGHT_CLICK_INTERACTIONS) {
            if (CoreUtils.hasInteraction(core, interaction.id)) {
                boolean bl = interaction.clickHandler.apply(player, player.getMainHandItem(), blockEntity);
                returnValue = !returnValue ? bl : returnValue;
            }
        }
        return returnValue;
    }

    public boolean reviveEntities(Item item, RespawnObeliskBlockEntity blockEntity, ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (!blockEntity.getItemTag().getCompound("RespawnObeliskData").contains("SavedEntities"))
            blockEntity.getItemTag().getCompound("RespawnObeliskData").put("SavedEntities", new ListTag());
        ListTag listTag = blockEntity.getItemTag().getCompound("RespawnObeliskData").getList("SavedEntities", 10);

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
                    if (player.level() instanceof ServerLevel serverLevel) {
                        Entity entity = serverLevel.getEntity(compound.getUUID("uuid"));
                        if ((entity == null || !entity.isAlive()) && blockEntity.getCharge(player) - ReviveConfig.revivalCost < 0) {
                            player.sendSystemMessage(Component.translatable("text.respawnobelisks.insufficient_charge"));
                            break;
                        }
                        if (entity != null && entity.isAlive())
                            continue;
                    }
                    Entity toSummon = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(compound.getString("type"))).create(player.level());
                    if (toSummon == null) continue;
                    toSummon.load(compound.getCompound("data"));
                    toSummon.setPos(pos.getX()+0.5, pos.getY()+2.5, pos.getZ()+0.5);
                    player.level().addFreshEntity(toSummon);
                    ModRegistries.reviveCriterion.trigger(player, toSummon);
                    if (toSummon instanceof Villager villager)
                        villager.getGossips().add(player.getUUID(), GossipType.MAJOR_POSITIVE, 40);
                    blockEntity.decreaseCharge(player, ReviveConfig.revivalCost);
                    hasFired = true;
                    count++;
                }
            }
            if (hasFired) {
                blockEntity.checkLimbo(true);
                ModPackets.CHANNEL.sendToPlayer(player, new PlayTotemAnimationPacket(item));
                if (!player.isCreative()) player.getMainHandItem().shrink(1);
                List<ServerPlayer> players = level.getPlayers(p -> getAABB(blockEntity.getBlockPos()).contains(p.getX(), p.getY(), p.getZ()));
                ModPackets.CHANNEL.sendToPlayers(players, new ParticleAnimationPacket("totem", player.getId(), pos));
            }
        }
        return true;
    }

    public InteractionResult takeCore(ServerPlayer player, RespawnObeliskBlockEntity blockEntity) {
        player.setItemInHand(InteractionHand.MAIN_HAND, blockEntity.getItemStack());
        blockEntity.setCoreInstance(ObeliskCore.Instance.EMPTY);
        blockEntity.checkLimbo(false);
        blockEntity.updateObeliskName();
        blockEntity.syncWithClient();
        ModPackets.CHANNEL.sendToPlayer(player, new PlaySoundPacket(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0f, 1.0f));
        return InteractionResult.SUCCESS;
    }

    public InteractionResult placeCore(ServerPlayer player, RespawnObeliskBlockEntity blockEntity, ResourceLocation location) {
        ItemStack toAdd = player.getMainHandItem().copy();
        toAdd.setCount(1);
        blockEntity.setCoreInstance(toAdd, ObeliskCore.CORES.get(location));
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
            if (!be.getItemStack().isEmpty()) // outdated code but idc (should be be.getCoreInstance()...)
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
            if (!blockEntity.getItemStack().isEmpty() && !player.isShiftKeyDown()) { // outdated code but idc (should be be.getCoreInstance()...)
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
        return ModRegistries.ROBE.get().create(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModRegistries.ROBE.get() ? (pLevel, pos, blockState, be) -> {
            if (be instanceof RespawnObeliskBlockEntity blockEntity)
                RespawnObeliskBlockEntity.tick(pLevel, pos, blockState, blockEntity);
        } : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel serverLevel, T blockEntity) {
        return blockEntity instanceof RespawnObeliskBlockEntity robe ? robe : null;
    }
}
