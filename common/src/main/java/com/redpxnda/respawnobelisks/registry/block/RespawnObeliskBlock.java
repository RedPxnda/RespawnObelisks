package com.redpxnda.respawnobelisks.registry.block;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
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
import net.minecraft.block.*;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.village.VillageGossipType;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.redpxnda.respawnobelisks.registry.ModRegistries.immortalityCurse;
import static com.redpxnda.respawnobelisks.util.ObeliskUtils.getAABB;

public class RespawnObeliskBlock extends Block implements BlockEntityProvider {
    public static final Function<RespawnObeliskBlockEntity, DispenserBehavior> DISPENSER_BEHAVIOR = (robe) -> (pointer, stack) -> {
        clickInteractions(null, robe, stack);
        return stack;
    };
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty WILD = BooleanProperty.of("wild");
    public static final DirectionProperty RESPAWN_SIDE = DirectionProperty.of("respawn_side");
    private static final VoxelShape HITBOX_BOTTOM_BASE = Block.createCuboidShape(1.5D, 1.0D, 1.5D, 14.5D, 32.0D, 14.5D);
    private static final VoxelShape HITBOX_BOTTOM_TRIM = Block.createCuboidShape(0D, 0D, 0D, 16D, 3D, 16D);
    private static final VoxelShape AABB_BOTTOM = VoxelShapes.union(HITBOX_BOTTOM_BASE, HITBOX_BOTTOM_TRIM);
    private static final VoxelShape HITBOX_TOP_BASE = Block.createCuboidShape(1.5D, -15.0D, 1.5D, 14.5D, 16.0D, 14.5D);
    private static final VoxelShape HITBOX_TOP_TRIM = Block.createCuboidShape(0D, -16D, 0D, 16D, -13D, 16D);
    private static final VoxelShape AABB_TOP = VoxelShapes.union(HITBOX_TOP_BASE, HITBOX_TOP_TRIM);

    public final @Nullable DimensionValidator dimension;

    public RespawnObeliskBlock(Settings pProperties, @Nullable DimensionValidator obeliskDimension) {
        super(pProperties);
        this.dimension = obeliskDimension;
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(HALF, DoubleBlockHalf.LOWER)
                .with(RESPAWN_SIDE, Direction.NORTH)
                .with(WILD, false)
        );
    }

    public boolean isTransparent(BlockState pState, BlockView pLevel, BlockPos pPos) {
        return true;
    }

    public float getAmbientOcclusionLightLevel(BlockState pState, BlockView pLevel, BlockPos pPos) {
        return 1.0F;
    }

    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return pState.get(HALF) == DoubleBlockHalf.LOWER ? AABB_BOTTOM : AABB_TOP;
    }

    public BlockState getStateForNeighborUpdate(BlockState pState, Direction pFacing, BlockState pFacingState, WorldAccess pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        DoubleBlockHalf doubleblockhalf = pState.get(HALF);
        if (pFacing.getAxis() == Direction.Axis.Y && doubleblockhalf == DoubleBlockHalf.LOWER == (pFacing == Direction.UP)) {
            return pFacingState.isOf(this) && pFacingState.get(HALF) != doubleblockhalf ? pState : Blocks.AIR.getDefaultState();
        } else {
            return doubleblockhalf == DoubleBlockHalf.LOWER && pFacing == Direction.DOWN && !pState.canPlaceAt(pLevel, pCurrentPos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
        }
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockPos blockpos = pContext.getBlockPos();
        World level = pContext.getWorld();
        if (blockpos.getY() < level.getTopY() - 1 && level.getBlockState(blockpos.up()).canReplace(pContext)) {
            return this.getDefaultState().with(HALF, DoubleBlockHalf.LOWER);
        } else {
            return null;
        }
    }
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        pLevel.setBlockState(pPos.up(), pState.with(HALF, DoubleBlockHalf.UPPER), 3);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HALF, RESPAWN_SIDE, WILD);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return state.get(HALF).equals(DoubleBlockHalf.LOWER);
    }

    @Override
    public int getComparatorOutput(BlockState state, World level, BlockPos pos) {
        if (state.get(HALF).equals(DoubleBlockHalf.LOWER) && level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity)
            return (int) (blockEntity.getCharge(null)*3f/20f);
        return 0;
    }

    public Optional<Vec3d> getRespawnLocation(BlockState state, BlockPos pos, ServerWorld level, ServerPlayerEntity player) {
        return getRespawnLocation(false, true, false, state, pos, level, player);
    }

    /**
     * @param isTeleport Whether this call is called by teleportation (recovery compass)
     * @param shouldCost Whether this call should actually take charge
     * @param forceCurse Whether the immortality curse should be automatically forced (usually used in tandem with isTeleport)
     * @return Calculated respawn position. Will be an empty {@link Optional} if player should be sent to world spawn.
     */
    public Optional<Vec3d> getRespawnLocation(boolean isTeleport, boolean shouldCost, boolean forceCurse, BlockState state, BlockPos pos, ServerWorld level, ServerPlayerEntity player) {
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.down();
            state = level.getBlockState(pos);
        }
        if ( // condition stuff
                level.getBlockEntity(pos) != null &&
                level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity && // make sure block entity is found
                !blockEntity.getCoreInstance().isEmpty() &&
                (RespawnObelisksConfig.INSTANCE.playerTrusting.allowObeliskRespawning || blockEntity.isPlayerTrusted(player.getEntityName()))
        ) {
            if (!RespawnObelisksConfig.INSTANCE.immortalityCurse.enableCurse) forceCurse = false; // override curse coercion

            double charge = blockEntity.getCharge(player);
            double cost = !shouldCost ? 0 : isTeleport ? RespawnObelisksConfig.INSTANCE.teleportation.teleportationCost : RespawnObelisksConfig.INSTANCE.radiance.respawnCost; // preparing cost value

            ObeliskInteraction.Manager startManager = new ObeliskInteraction.Manager(forceCurse, shouldCost, cost, null);
            for (ObeliskInteraction i : ObeliskInteraction.RESPAWN_INTERACTIONS.get(ObeliskInteraction.Injection.START)) { // Obelisk Respawn Interactions, for the start injection point
                i.respawnHandler.accept(player, blockEntity, startManager);
            }
            cost = startManager.cost;
            forceCurse = startManager.curseForced;
            shouldCost = startManager.shouldConsumeCost;

            if (charge-cost >= 0 && shouldCost && !forceCurse) player.removeStatusEffect(immortalityCurse.get()); // remove curse if charge

            StatusEffectInstance mei = null;
            if ((!RespawnObelisksConfig.INSTANCE.immortalityCurse.enableCurse && charge - (RespawnObelisksConfig.INSTANCE.radiance.forgivingRespawn ? 0 : cost) <= 0) || (player.hasStatusEffect(immortalityCurse.get()) && (mei = player.getStatusEffect(immortalityCurse.get())).getAmplifier() >= RespawnObelisksConfig.INSTANCE.immortalityCurse.curseMaxLevel-1))
                return Optional.empty(); // if curse level is over the max, or no charge and curse is disabled, send to spawn

            boolean hasPlayedCurseAnim = false;
            if (RespawnObelisksConfig.INSTANCE.immortalityCurse.enableCurse && (charge-cost < 0 || forceCurse) && shouldCost) { // if cost brings charge below 0, curse
                boolean applyCurse = isTeleport || player.isAlive();
                int amplifier = applyCurse ?
                        (
                                mei != null ?
                                        Math.min(mei.getAmplifier()+RespawnObelisksConfig.INSTANCE.immortalityCurse.curseLevelIncrement, RespawnObelisksConfig.INSTANCE.immortalityCurse.curseMaxLevel-1) :
                                        RespawnObelisksConfig.INSTANCE.immortalityCurse.curseLevelIncrement-1
                        ) : RespawnObelisksConfig.INSTANCE.immortalityCurse.curseMaxLevel+1; // may seem odd to do 1 more than the curse max level, but see the clone handler in CommonEvents to understand
                if (!player.hasStatusEffect(ModRegistries.immortalityCurse.get()) || applyCurse) // I would add another check for 'forceCurse' here, but it can cause issues if this method is used incorrectly.
                    player.setStatusEffect(
                            new StatusEffectInstance(
                                    ModRegistries.immortalityCurse.get(),
                                    RespawnObelisksConfig.INSTANCE.immortalityCurse.curseDuration,
                                    amplifier
                            ), null
                    );
                ObeliskUtils.curseHandler(level, player, pos, state); // curse animation and whatnot
                hasPlayedCurseAnim = true;
            }

            if (shouldCost && !hasPlayedCurseAnim)
                blockEntity.chargeAndAnimate(player, -cost); // actually charge
            else if (shouldCost)
                blockEntity.decreaseCharge(player, cost); // unfortunately, skipping the animation stuff means the lastRespawn value doesn't get updated during curse respawns

            BlockPos spawnPos = pos.offset(state.get(RESPAWN_SIDE));
            Vec3d vec = new Vec3d(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

            ObeliskInteraction.Manager endManager = new ObeliskInteraction.Manager(forceCurse, shouldCost, cost, vec);
            for (ObeliskInteraction i : ObeliskInteraction.RESPAWN_INTERACTIONS.get(ObeliskInteraction.Injection.END)) { // Obelisk Respawn Interactions, for the end injection point
                i.respawnHandler.accept(player, blockEntity, endManager);
            }
            vec = endManager.getSpawnLoc();
            shouldCost = startManager.shouldConsumeCost;

            if (shouldCost) ModRegistries.respawnCriterion.trigger(player); // achievement bs
            return Optional.of(vec);
        }
        return Optional.empty();
    }

    public ActionResult onUse(BlockState state, World pLevel, BlockPos pos, PlayerEntity pPlayer, Hand hand, BlockHitResult hitResult) {
        if (pLevel.isClient) {
            return ActionResult.CONSUME;
        } else {
            if (state.get(HALF) == DoubleBlockHalf.UPPER) {
                pos = pos.down();
                state = pLevel.getBlockState(pos);
            }
            if (pPlayer instanceof ServerPlayerEntity player && pLevel instanceof ServerWorld level && level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity && (RespawnObelisksConfig.INSTANCE.playerTrusting.allowObeliskInteraction || blockEntity.isPlayerTrusted(player.getEntityName()))) {
                if (pPlayer.getMainHandStack().isOf(Items.BEDROCK) && pPlayer.getOffHandStack().isOf(Items.TALL_GRASS)) { // for wild obelisk setup
                    pLevel.setBlockState(pos, state.with(WILD, true), 3);
                    blockEntity.hasRandomCharge = true;
                    return ActionResult.SUCCESS;
                }

                double charge = blockEntity.getCharge(player);
                // exploding if wrong dimension
                if (dimension != null && !dimension.isValid(level, state, pos, blockEntity, player)) {
                    level.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    level.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), 3);
                    level.setBlockState(pos.down(), Blocks.AIR.getDefaultState(), 3);
                    level.createExplosion(null, level.getDamageSources().badRespawnPoint(pos.toCenterPos()), null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F, true, World.ExplosionSourceType.BLOCK);
                    ModRegistries.kaboomCriterion.trigger(player);
                    return ActionResult.SUCCESS;
                }

                if (player.isSneaking() && player.getMainHandStack().isEmpty() && !blockEntity.getItemStack().isEmpty())
                    return takeCore(player, blockEntity);
                Identifier rl;
                if (blockEntity.getItemStack().isEmpty() && ObeliskCore.CORES.containsKey(rl = Registries.ITEM.getId(player.getMainHandStack().getItem())))
                    return placeCore(player, blockEntity, rl);

                // right click interactions
                if (clickInteractions(player, blockEntity, player.getMainHandStack())) return ActionResult.SUCCESS;

                Item revivalItem = RespawnObelisksConfig.INSTANCE.revival.revivalItem; // to-do: make into interaction
                if (
                        CoreUtils.hasInteraction(blockEntity.getCoreInstance(), ObeliskInteraction.REVIVE) &&
                        player.getMainHandStack().getItem() == revivalItem &&
                        !player.getItemCooldownManager().isCoolingDown(player.getMainHandStack().getItem()) &&
                        !blockEntity.getItemNbt().isEmpty() &&
                        blockEntity.getItemNbt().contains("tag") &&
                        blockEntity.getItemNbt().getCompound("tag").contains("RespawnObeliskData")
                ) {
                    if (reviveEntities(revivalItem, blockEntity, player, level, pos)) return ActionResult.SUCCESS;
                } else if (blockEntity.getCoreInstance().isEmpty())
                    player.sendMessage(Text.translatable("text.respawnobelisks.no_core"));
                else if (charge <= 0 && !RespawnObelisksConfig.INSTANCE.radiance.allowEmptySpawnSetting)
                    player.sendMessage(Text.translatable("text.respawnobelisks.no_charge"));
                else { // Setting spawn point
                    int degrees = 90; // degrees for respawn
                    if (state.get(RESPAWN_SIDE) == Direction.NORTH) degrees = 180;
                    else if (state.get(RESPAWN_SIDE) == Direction.EAST) degrees = -90;
                    else if (state.get(RESPAWN_SIDE) == Direction.SOUTH) degrees = 0;
                    if ((player.getSpawnPointPosition() != null && !player.getSpawnPointPosition().equals(pos)) || player.getSpawnPointPosition() == null) {
                        List<ServerPlayerEntity> players = level.getPlayers(p -> getAABB(blockEntity.getPos()).contains(p.getX(), p.getY(), p.getZ()));
                        ModPackets.CHANNEL.sendToPlayers(players, new PlaySoundPacket(Registries.SOUND_EVENT.getOrEmpty(new Identifier(RespawnObelisksConfig.INSTANCE.radiance.spawnSettingSound)).orElse(SoundEvents.UI_BUTTON_CLICK.value()), 1f, 1f));
                    }
                    player.setSpawnPoint(level.getRegistryKey(), pos, degrees, false, true);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.FAIL;
    }

    public static boolean clickInteractions(@Nullable PlayerEntity player, RespawnObeliskBlockEntity blockEntity, ItemStack stack) {
        if (player != null && player.getItemCooldownManager().isCoolingDown(player.getMainHandStack().getItem())) return false;
        ObeliskCore.Instance core = blockEntity.getCoreInstance();
        boolean returnValue = false;
        for (ObeliskInteraction interaction : ObeliskInteraction.RIGHT_CLICK_INTERACTIONS) {
            if (CoreUtils.hasInteraction(core, interaction.id)) {
                boolean bl = interaction.clickHandler.apply(player, stack, blockEntity);
                returnValue = !returnValue ? bl : returnValue;
            }
        }
        return returnValue;
    }

    public boolean reviveEntities(Item item, RespawnObeliskBlockEntity blockEntity, ServerPlayerEntity player, ServerWorld level, BlockPos pos) {
        if (!blockEntity.getItemTag().getCompound("RespawnObeliskData").contains("SavedEntities"))
            blockEntity.getItemTag().getCompound("RespawnObeliskData").put("SavedEntities", new NbtList());
        NbtList listTag = blockEntity.getItemTag().getCompound("RespawnObeliskData").getList("SavedEntities", 10);

        if (!listTag.isEmpty()) {
            boolean hasFired = false;
            int count = 0;
            for (NbtElement tag : listTag) {
                if (count >= RespawnObelisksConfig.INSTANCE.revival.maxEntities) break;
                if (
                        tag instanceof NbtCompound compound &&
                                compound.contains("uuid") &&
                                compound.contains("type") &&
                                compound.contains("data")
                ) {
                    if (player.getWorld() instanceof ServerWorld serverLevel) {
                        Entity entity = serverLevel.getEntity(compound.getUuid("uuid"));
                        if ((entity == null || !entity.isAlive()) && blockEntity.getCharge(player) - RespawnObelisksConfig.INSTANCE.revival.revivalCost < 0) {
                            player.sendMessage(Text.translatable("text.respawnobelisks.insufficient_charge"));
                            break;
                        }
                        if (entity != null && entity.isAlive())
                            continue;
                    }
                    Entity toSummon = Registries.ENTITY_TYPE.get(Identifier.tryParse(compound.getString("type"))).create(player.getWorld());
                    if (toSummon == null) continue;
                    toSummon.readNbt(compound.getCompound("data"));
                    toSummon.setPosition(pos.getX()+0.5, pos.getY()+2.5, pos.getZ()+0.5);
                    toSummon.addCommandTag("respawnobelisks:no_drops_entity");
                    player.getWorld().spawnEntity(toSummon);
                    ModRegistries.reviveCriterion.trigger(player, toSummon);
                    if (toSummon instanceof VillagerEntity villager)
                        villager.getGossip().startGossip(player.getUuid(), VillageGossipType.MAJOR_POSITIVE, 40);
                    blockEntity.decreaseCharge(player, RespawnObelisksConfig.INSTANCE.revival.revivalCost);
                    hasFired = true;
                    count++;
                }
            }
            if (hasFired) {
                blockEntity.checkLimbo(true);
                ModPackets.CHANNEL.sendToPlayer(player, new PlayTotemAnimationPacket(item));
                if (!player.isCreative()) player.getMainHandStack().decrement(1);
                List<ServerPlayerEntity> players = level.getPlayers(p -> getAABB(blockEntity.getPos()).contains(p.getX(), p.getY(), p.getZ()));
                ModPackets.CHANNEL.sendToPlayers(players, new ParticleAnimationPacket("totem", player.getId(), pos));
            }
        }
        return true;
    }

    public ActionResult takeCore(ServerPlayerEntity player, RespawnObeliskBlockEntity blockEntity) {
        player.setStackInHand(Hand.MAIN_HAND, blockEntity.getItemStack());
        blockEntity.setCoreInstance(ObeliskCore.Instance.EMPTY);
        blockEntity.checkLimbo(false);
        blockEntity.updateObeliskName();
        blockEntity.syncWithClient();
        ModPackets.CHANNEL.sendToPlayer(player, new PlaySoundPacket(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f));
        return ActionResult.SUCCESS;
    }

    public ActionResult placeCore(ServerPlayerEntity player, RespawnObeliskBlockEntity blockEntity, Identifier location) {
        ItemStack toAdd = player.getMainHandStack().copy();
        toAdd.setCount(1);
        blockEntity.setCoreInstance(toAdd, ObeliskCore.CORES.get(location));
        blockEntity.checkLimbo(false);
        blockEntity.updateObeliskName();
        blockEntity.syncWithClient();
        player.getMainHandStack().decrement(1);
        ModPackets.CHANNEL.sendToPlayer(player, new PlaySoundPacket(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f));
        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState blockState, World level, BlockPos pos, BlockState newBlockState, boolean isMoving) {
        if (blockState.isOf(newBlockState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RespawnObeliskBlockEntity be) {
            if (!be.getItemStack().isEmpty()) // outdated code but idc (should be be.getCoreInstance()...)
                ItemScatterer.spawn(level, pos.getX()+0.5, pos.getY(), pos.getZ()+0.5, be.getItemStack());
        }
        super.onStateReplaced(blockState, level, pos, newBlockState, isMoving);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView getter, BlockPos pos) {
        if (state.get(HALF).equals(DoubleBlockHalf.UPPER))
            pos = pos.down();
        BlockEntity be = getter.getBlockEntity(pos);
        if (be instanceof RespawnObeliskBlockEntity blockEntity) {
            if (!RespawnObelisksConfig.INSTANCE.playerTrusting.allowObeliskBreaking && !blockEntity.isPlayerTrusted(player.getEntityName())) {
                if (player instanceof ServerPlayerEntity serverPlayer) serverPlayer.sendMessageToClient(Text.translatable("text.respawnobelisks.untrusted"), true);
                return 0f;
            }
            if (!blockEntity.getItemStack().isEmpty() && !player.isSneaking()) { // outdated code but idc (should be be.getCoreInstance()...)
                if (player instanceof ServerPlayerEntity serverPlayer) serverPlayer.sendMessageToClient(Text.translatable("text.respawnobelisks.has_core"), true);
                return 0f;
            }
            if (blockEntity.hasTeleportingEntity) {
                if (player instanceof ServerPlayerEntity serverPlayer) serverPlayer.sendMessageToClient(Text.translatable("text.respawnobelisks.wormhole_open"), true);
                return 0f;
            }
        }
        return super.calcBlockBreakingDelta(state, player, getter, pos);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        if (pState.get(HALF).equals(DoubleBlockHalf.UPPER)) return null;
        return ModRegistries.ROBE.get().instantiate(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState state, BlockEntityType<T> type) {
        return type == ModRegistries.ROBE.get() ? (pLevel, pos, blockState, be) -> {
            if (be instanceof RespawnObeliskBlockEntity blockEntity)
                RespawnObeliskBlockEntity.tick(pLevel, pos, blockState, blockEntity);
        } : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getGameEventListener(ServerWorld serverLevel, T blockEntity) {
        return blockEntity instanceof RespawnObeliskBlockEntity robe ? robe : null;
    }
}
