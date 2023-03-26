package com.redpxnda.respawnobelisks.registry.block;

import com.mojang.datafixers.util.Either;
import com.redpxnda.respawnobelisks.config.ServerConfig;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.PlaySoundPacket;
import com.redpxnda.respawnobelisks.network.RespawnObeliskInteractionPacket;
import com.redpxnda.respawnobelisks.network.RespawnObeliskSecondaryInteractionPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.particle.ParticlePack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    public final Either<ResourceKey<Level>, String> OBELISK_DIMENSION;
    public final Item CORE_ITEM;

    public RespawnObeliskBlock(Properties pProperties, Either<ResourceKey<Level>, String> obeliskDimension, Item coreItem) {
        super(pProperties);
        this.OBELISK_DIMENSION = obeliskDimension;
        this.CORE_ITEM = coreItem;
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
        if (state.getValue(HALF).equals(DoubleBlockHalf.LOWER) && level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity)
            return (int) (blockEntity.getCharge()*3f/20f);
        return 0;
    }

    public Optional<Vec3> getRespawnLocation(BlockState state, BlockPos pos, ServerLevel level, ServerPlayer player) {
        if (level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity) {
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                pos = pos.below();
                state = level.getBlockState(pos);
            }
            double charge = blockEntity.getCharge();
            if (!(charge > 0 || player.hasEffect(ModRegistries.IMMORTALITY_CURSE.get()))) {
                if (ServerConfig.enableCurse)
                    player.addEffect(new MobEffectInstance(ModRegistries.IMMORTALITY_CURSE.get(), ServerConfig.curseDuration, 0));
            }
            if (charge > 0) player.removeEffect(ModRegistries.IMMORTALITY_CURSE.get());
            Optional<Block> blockHolder = Registry.BLOCK.getOptional(new ResourceLocation(ServerConfig.infiniteChargeBlock));
            if (blockHolder.isPresent() && charge > 0) {
                Class<? extends Block> blockClass = blockHolder.get().getClass();
                boolean isNegative = !blockClass.isInstance(level.getBlockState(pos.below()).getBlock());
                if (isNegative) {
                    blockEntity.decreaseCharge(ServerConfig.obeliskDepleteAmount, blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState());
                    blockEntity.setLastRespawn(level.getGameTime());
                }
                AABB aabb = AABB.of(new BoundingBox(
                        player.getBlockX()-10, player.getBlockY()-10, player.getBlockZ()-10,
                        player.getBlockX()+10, player.getBlockY()+10, player.getBlockZ()+10
                ));
                List<ServerPlayer> players = level.getPlayers(p -> aabb.contains(p.getX(), p.getY(), p.getZ()));
                ModPackets.CHANNEL.sendToPlayers(players, new RespawnObeliskSecondaryInteractionPacket(player.getId(), state.getValue(PACK), pos, isNegative, false));
                if (isNegative) state.getValue(PACK).particleHandler.depleteServerHandler(level, player, pos);
                else state.getValue(PACK).particleHandler.chargeServerHandler(level, player, pos);
            } else {
                if (ServerConfig.enableCurse) {
                    AABB aabb = AABB.of(new BoundingBox(
                            player.getBlockX()-10, player.getBlockY()-10, player.getBlockZ()-10,
                            player.getBlockX()+10, player.getBlockY()+10, player.getBlockZ()+10
                    ));
                    List<ServerPlayer> players = level.getPlayers(p -> aabb.contains(p.getX(), p.getY(), p.getZ()));
                    ModPackets.CHANNEL.sendToPlayers(players, new RespawnObeliskInteractionPacket(player.getId(), state.getValue(PACK), pos, true));
                    state.getValue(PACK).particleHandler.curseServerHandler(level, player, pos);
                }
            }
            boolean isNotCurseEnabled = !ServerConfig.enableCurse;
            MobEffectInstance MEI = null;
            if (player.hasEffect(ModRegistries.IMMORTALITY_CURSE.get()))
                MEI = player.getEffect(ModRegistries.IMMORTALITY_CURSE.get());
            boolean isMEI = MEI == null;
            boolean isNotFullyCursed = ServerConfig.enableCurse && (isMEI || MEI.getAmplifier() < ServerConfig.curseMaxLevel - 1);
            if (isNotCurseEnabled || isNotFullyCursed || charge > 0) {
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
            if (pPlayer instanceof ServerPlayer player && pLevel instanceof ServerLevel level && level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity) {
                double charge = blockEntity.getCharge();
                if (OBELISK_DIMENSION.left().isPresent()) {
                    if (level.dimension() != OBELISK_DIMENSION.left().get()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
                        level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 3);
                        level.setBlock(pos.below(2), Blocks.AIR.defaultBlockState(), 3);
                        level.explode(null, DamageSource.badRespawnPointExplosion(), null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F, true, Explosion.BlockInteraction.DESTROY);
                        return InteractionResult.SUCCESS;
                    }
                }

                if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && !blockEntity.getItemNbt().isEmpty()) {
                    if (!blockEntity.getItemNbt().isEmpty()) player.setItemInHand(InteractionHand.MAIN_HAND, blockEntity.getItemStack());
                    blockEntity.setItemNbt(new CompoundTag());
                    blockEntity.syncWithClient(level, pos, state);
                    ModPackets.CHANNEL.sendToPlayer(player, new PlaySoundPacket(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0f, 1.0f));

                    return InteractionResult.SUCCESS;
                }
                if (player.getMainHandItem().getItem().equals(CORE_ITEM) && player.getMainHandItem().hasTag() && player.getMainHandItem().getOrCreateTag().contains("RespawnObeliskData")) {
                    ItemStack toAdd = player.getMainHandItem();
                    toAdd.setCount(1);
                    blockEntity.setItem(toAdd);
                    blockEntity.syncWithClient(level, pos, state);
                    player.getMainHandItem().setCount(player.getMainHandItem().getCount()-1);
                    ModPackets.CHANNEL.sendToPlayer(player, new PlaySoundPacket(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0f, 1.0f));

                    return InteractionResult.SUCCESS;
                }
                String[] CHARGE_ITEMS = ServerConfig.obeliskChargeItems;
                if (OBELISK_DIMENSION.left().isPresent()) {
                    if (OBELISK_DIMENSION.left().get().equals(Level.NETHER)) CHARGE_ITEMS = ServerConfig.netherObeliskChargeItems;
                    else if (OBELISK_DIMENSION.left().get().equals(Level.END)) CHARGE_ITEMS = ServerConfig.endObeliskChargeItems;
                }
                // Charging obelisk
                for (String str : CHARGE_ITEMS) {
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
                        for (String str2 : CHARGE_ITEMS) {
                            Optional<Item> itemHolder2 = Registry.ITEM.getOptional(new ResourceLocation(str2.split("\\|")[0]));
                            itemHolder2.ifPresent(holder -> player.getCooldowns().addCooldown(holder, 30));
                        }
                        AABB aabb = AABB.of(new BoundingBox(
                                player.getBlockX()-10, player.getBlockY()-10, player.getBlockZ()-10,
                                player.getBlockX()+10, player.getBlockY()+10, player.getBlockZ()+10
                        ));
                        List<ServerPlayer> players = level.getPlayers(p -> aabb.contains(p.getX(), p.getY(), p.getZ()));
                        ModPackets.CHANNEL.sendToPlayers(players, new RespawnObeliskSecondaryInteractionPacket(player.getId(), state.getValue(PACK), pos, (chargeAmount < 0), false));
                        if (chargeAmount < 0) state.getValue(PACK).particleHandler.depleteServerHandler(level, player, pos);
                        else state.getValue(PACK).particleHandler.chargeServerHandler(level, player, pos);
                        blockEntity.increaseCharge(chargeAmount, blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState());
                        blockEntity.setLastCharge(level.getGameTime());
                        if (!player.isCreative()) player.getMainHandItem().setCount(player.getMainHandItem().getCount()-1);
                        return InteractionResult.SUCCESS;
                    }
                }
                // block removal
                Optional<Item> itemHolder = Registry.ITEM.getOptional(new ResourceLocation(ServerConfig.removalItem));
                if (ServerConfig.allowPickup && itemHolder.isPresent() && player.getMainHandItem().getItem() == itemHolder.get()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
                    if (!player.isCreative()) player.getMainHandItem().setCount(player.getMainHandItem().getCount()-1);
                    AABB aabb = AABB.of(new BoundingBox(
                            player.getBlockX()-10, player.getBlockY()-10, player.getBlockZ()-10,
                            player.getBlockX()+10, player.getBlockY()+10, player.getBlockZ()+10
                    ));
                    List<ServerPlayer> players = level.getPlayers(p -> aabb.contains(p.getX(), p.getY(), p.getZ()));
                    ModPackets.CHANNEL.sendToPlayers(players, new RespawnObeliskInteractionPacket(player.getId(), state.getValue(PACK), pos, false));
                    if (ServerConfig.allowPickup) {
                        ItemStack stack = new ItemStack(CORE_ITEM.arch$holder());
                        ItemEntity entity = new ItemEntity(level, pos.getX(), pos.getY()+0.5, pos.getZ(), stack);
                        level.addFreshEntity(entity);
                    }
                    return InteractionResult.SUCCESS;
                    // checking if there's no charge
                } else if (charge == 0) {
                    player.sendSystemMessage(Component.translatable("text.respawnobelisks.no_charge"));
                } else {
                    int degrees;
                    if (state.getValue(RESPAWN_SIDE) == Direction.NORTH) degrees = 180;
                    else if (state.getValue(RESPAWN_SIDE) == Direction.EAST) degrees = -90;
                    else if (state.getValue(RESPAWN_SIDE) == Direction.SOUTH) degrees = 0;
                    else degrees = 90;
                    if ((player.getRespawnPosition() != null && !player.getRespawnPosition().equals(pos)) || player.getRespawnPosition() == null) {
                        AABB aabb = AABB.of(new BoundingBox(
                                player.getBlockX()-10, player.getBlockY()-10, player.getBlockZ()-10,
                                player.getBlockX()+10, player.getBlockY()+10, player.getBlockZ()+10
                        ));
                        List<ServerPlayer> players = level.getPlayers(p -> aabb.contains(p.getX(), p.getY(), p.getZ()));
                        ModPackets.CHANNEL.sendToPlayers(players, new RespawnObeliskSecondaryInteractionPacket(player.getId(), state.getValue(PACK), pos, false, true));
                    }
                    player.setRespawnPosition(level.dimension(), pos, degrees, false, true);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
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
