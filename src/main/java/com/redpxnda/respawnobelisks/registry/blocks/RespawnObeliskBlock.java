package com.redpxnda.respawnobelisks.registry.blocks;

import com.mojang.datafixers.util.Either;
import com.redpxnda.respawnobelisks.config.ServerConfig;
import com.redpxnda.respawnobelisks.network.RespawnObeliskSecondaryInteractionPacket;
import com.redpxnda.respawnobelisks.network.RespawnObeliskInteractionPacket;
import com.redpxnda.respawnobelisks.network.Packets;
import com.redpxnda.respawnobelisks.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class RespawnObeliskBlock extends Block {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 3);
    public static final DirectionProperty RESPAWN_SIDE = DirectionProperty.create("respawn_side");
    private static final VoxelShape HITBOX_BOTTOM_BASE = Block.box(1.5D, 1.0D, 1.5D, 14.5D, 32.0D, 14.5D);
    private static final VoxelShape HITBOX_BOTTOM_TRIM = Block.box(0D, 0D, 0D, 16D, 3D, 16D);
    private static final VoxelShape AABB_BOTTOM = Shapes.or(HITBOX_BOTTOM_BASE, HITBOX_BOTTOM_TRIM);
    private static final VoxelShape HITBOX_TOP_BASE = Block.box(1.5D, -15.0D, 1.5D, 14.5D, 16.0D, 14.5D);
    private static final VoxelShape HITBOX_TOP_TRIM = Block.box(0D, -16D, 0D, 16D, -13D, 16D);
    private static final VoxelShape AABB_TOP = Shapes.or(HITBOX_TOP_BASE, HITBOX_TOP_TRIM);
    public final Either<ResourceKey<Level>, String> OBELISK_DIMENSION;

    private ForgeConfigSpec.ConfigValue<List<String>> CHARGE_ITEMS = ServerConfig.OBELISK_CHARGE_ITEM;

    public RespawnObeliskBlock(Properties pProperties, Either<ResourceKey<Level>, String> obeliskDimension) {
        super(pProperties);
        this.OBELISK_DIMENSION = obeliskDimension;
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(CHARGE, 0).setValue(RESPAWN_SIDE, Direction.NORTH));
    }

    public RespawnObeliskBlock setChargeItems(ForgeConfigSpec.ConfigValue<List<String>> chargeItems) {
        this.CHARGE_ITEMS = chargeItems;
        return this;
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
        pBuilder.add(HALF, CHARGE, RESPAWN_SIDE);
    }

    public Optional<Vec3> getRespawnLocation(BlockState state, BlockPos pos, ServerLevel level, ServerPlayer player) {
        if (!(state.getValue(CHARGE) > 0 || player.hasEffect(Registry.IMMORTALITY_CURSE.get())))
            if (ServerConfig.ENABLE_CURSE.get()) player.addEffect(new MobEffectInstance(Registry.IMMORTALITY_CURSE.get(), ServerConfig.CURSE_DURATION.get(), 0));
        if (state.getValue(CHARGE) > 0) player.removeEffect(Registry.IMMORTALITY_CURSE.get());
        Optional<Holder<Block>> blockHolder = ForgeRegistries.BLOCKS.getHolder(new ResourceLocation(ServerConfig.INFINITE_CHARGE_BLOCK.get()));
        if (blockHolder.isPresent() && state.getValue(RespawnObeliskBlock.CHARGE) > 0) {
            Class<? extends Block> blockClass = blockHolder.get().get().getClass();
            boolean isNegative = !blockClass.isInstance(level.getBlockState(pos.below()).getBlock());
            if (isNegative) {
                level.setBlock(pos, state.setValue(CHARGE, state.getValue(CHARGE) - 1).setValue(HALF, DoubleBlockHalf.LOWER), 3);
                level.setBlock(pos.above(), state.setValue(CHARGE, state.getValue(CHARGE) - 1).setValue(HALF, DoubleBlockHalf.UPPER), 3);
            }
            Packets.sendToPlayer(new RespawnObeliskSecondaryInteractionPacket(ServerConfig.OBELISK_DEPLETE_SOUND.get(), pos, isNegative, false), player);
        } else {
            if (ServerConfig.ENABLE_CURSE.get()) Packets.sendToPlayer(new RespawnObeliskInteractionPacket(ServerConfig.CURSE_SOUND.get(), pos, true), player);
        }
        boolean isNotCurseEnabled = !ServerConfig.ENABLE_CURSE.get();
        MobEffectInstance MEI = null;
        if (player.hasEffect(Registry.IMMORTALITY_CURSE.get()))
            MEI = player.getEffect(Registry.IMMORTALITY_CURSE.get());
        boolean isMEI = MEI == null;
        boolean isNotFullyCursed = ServerConfig.ENABLE_CURSE.get() && (isMEI || MEI.getAmplifier() < ServerConfig.MAX_CURSE_LEVEL.get()-1);
        if (isNotCurseEnabled || isNotFullyCursed || state.getValue(CHARGE) > 0) {
            BlockPos spawnPos = pos.relative(state.getValue(RESPAWN_SIDE));
            return Optional.of(new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5));
        }
        return Optional.empty();
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.CONSUME;
        } else {
            if (pPlayer instanceof ServerPlayer player) {
                BlockPos finalPPos = pPos;
                if (OBELISK_DIMENSION.left().isPresent()) {
                    if (pLevel.dimension() != OBELISK_DIMENSION.left().get()) {
                        pLevel.setBlock(finalPPos, Blocks.AIR.defaultBlockState(), 3);
                        pLevel.setBlock(finalPPos.above(), Blocks.AIR.defaultBlockState(), 3);
                        pLevel.setBlock(finalPPos.below(), Blocks.AIR.defaultBlockState(), 3);
                        pLevel.setBlock(finalPPos.below().below(), Blocks.AIR.defaultBlockState(), 3);
                        pLevel.explode(null, DamageSource.badRespawnPointExplosion(), null, finalPPos.getX() + 0.5D, finalPPos.getY() + 0.5D, finalPPos.getZ() + 0.5D, 5.0F, true, Explosion.BlockInteraction.DESTROY);
                        return InteractionResult.SUCCESS;
                    }
                }
                // Charging obelisk
                for (String str : CHARGE_ITEMS.get()) {
                    String[] sections = str.split("\\|"); // "minecraft:item|17|true" is str
                    int chargeAmount = Integer.parseInt(sections[1]); // 17 ^
                    boolean allowOverfill = sections.length >= 3 && Boolean.parseBoolean(sections[2]); // true ^
                    Optional<Holder<Item>> itemHolder = ForgeRegistries.ITEMS.getHolder(new ResourceLocation(sections[0]));
                    boolean fullyCharged = pState.getValue(CHARGE) == 3 && chargeAmount > 0;
                    boolean empty = pState.getValue(CHARGE) == 0 && chargeAmount < 0;
                    if (itemHolder.isPresent() &&
                            !player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem()) &&
                            player.getMainHandItem().getItem() == itemHolder.get().get() // if held item = item
                            && (pState.getValue(CHARGE) + chargeAmount <= 3 || allowOverfill) // if charge would go over 3, dont allow unless config says so
                            && (pState.getValue(CHARGE) + chargeAmount >= 0 || allowOverfill) // if charge would go under 0, dont allow unless config says so
                            && !fullyCharged // if fully charged, don't allow more
                            && !empty // if empty charged, don't allow less
                    ) {
                        for (String str2 : CHARGE_ITEMS.get()) {
                            Optional<Holder<Item>> itemHolder2 = ForgeRegistries.ITEMS.getHolder(new ResourceLocation(str2.split("\\|")[0]));
                            itemHolder2.ifPresent(holder -> player.getCooldowns().addCooldown(holder.get(), 30));
                        }
                        Packets.sendToPlayer(new RespawnObeliskSecondaryInteractionPacket((chargeAmount < 0 ? ServerConfig.OBELISK_DEPLETE_SOUND.get() : ServerConfig.OBELISK_CHARGE_SOUND.get()), (pState.getValue(HALF) == DoubleBlockHalf.UPPER ? pPos.below() : pPos), (chargeAmount < 0), false), player);
                        BlockState newState = pState.getValue(CHARGE) + chargeAmount >= 0 ?
                                (pState.getValue(CHARGE) + chargeAmount <= 3 ? pState.setValue(CHARGE, pState.getValue(CHARGE) + chargeAmount) : pState.setValue(CHARGE, 3))
                                : pState.setValue(CHARGE, 0);
                        DoubleBlockHalf half;
                        BlockPos offsetPos;
                        if (pState.getValue(HALF) == DoubleBlockHalf.LOWER) {
                            half = DoubleBlockHalf.UPPER;
                            offsetPos = pPos.above();
                        } else {
                            half = DoubleBlockHalf.LOWER;
                            offsetPos = pPos.below();
                        }
                        pLevel.setBlock(pPos, newState, 3);
                        pLevel.setBlock(offsetPos, newState.setValue(HALF, half), 3);
                        if (!player.isCreative()) player.getMainHandItem().setCount(player.getMainHandItem().getCount()-1);
                        return InteractionResult.SUCCESS;
                    }
                }
                // block removal
                Optional<Holder<Item>> itemHolder = ForgeRegistries.ITEMS.getHolder(new ResourceLocation(ServerConfig.REMOVAL_ITEM.get()));
                if (ServerConfig.ALLOW_PICKUP.get() && itemHolder.isPresent() && player.getMainHandItem().getItem() == itemHolder.get().get()) {
                    if (pState.getValue(HALF) == DoubleBlockHalf.UPPER) pPos = pPos.below();
                    pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
                    pLevel.setBlock(pPos.above(), Blocks.AIR.defaultBlockState(), 3);
                    if (!player.isCreative()) player.getMainHandItem().setCount(player.getMainHandItem().getCount()-1);
                    Packets.sendToPlayer(new RespawnObeliskInteractionPacket(ServerConfig.OBELISK_REMOVAL_SOUND.get(), pPos, false), player);
                    if (ServerConfig.ALLOW_PICKUP.get()) {
                        Optional<Holder<Item>> obeliskHolder = Registry.RESPAWN_OBELISK_ITEM.getHolder();
                        if (obeliskHolder.isPresent()) {
                            ItemStack stack = new ItemStack(obeliskHolder.get());
                            ItemEntity entity = new ItemEntity(pLevel, pPos.getX(), pPos.getY()+0.5, pPos.getZ(), stack);
                            pLevel.addFreshEntity(entity);
                        }
                    }
                    return InteractionResult.SUCCESS;
                // checking if there's no charge
                } else if (pState.getValue(CHARGE) == 0) {
                    player.sendSystemMessage(Component.translatable("text.respawnobelisks.no_charge"));
                } else {
                    int degrees;
                    if (pState.getValue(RESPAWN_SIDE) == Direction.NORTH) degrees = 180;
                    else if (pState.getValue(RESPAWN_SIDE) == Direction.EAST) degrees = -90;
                    else if (pState.getValue(RESPAWN_SIDE) == Direction.SOUTH) degrees = 0;
                    else degrees = 90;
                    if (!(pState.getValue(HALF) == DoubleBlockHalf.LOWER)) pPos = pPos.below();
                    if ((player.getRespawnPosition() != null && !player.getRespawnPosition().equals(pPos)) || player.getRespawnPosition() == null)
                        Packets.sendToPlayer(new RespawnObeliskSecondaryInteractionPacket(ServerConfig.OBELISK_SET_RESPAWN_SOUND.get(), pPos, false, true), player);
                    player.setRespawnPosition(pLevel.dimension(), pPos, degrees, false, true);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }
}
