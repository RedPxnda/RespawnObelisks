package com.redpxnda.respawnobelisks.registry.item;

import com.redpxnda.nucleus.util.PlayerUtil;
import com.redpxnda.respawnobelisks.RespawnObelisks;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import com.redpxnda.respawnobelisks.data.saved.RuneCircles;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public class BoundCompassItem extends CompassItem {
    private static final Logger LOGGER = RespawnObelisks.getLogger();

    public BoundCompassItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext useOnContext) {
        if (!RespawnObelisksConfig.INSTANCE.teleportation.enableTeleportation) return ActionResult.FAIL;
        if (useOnContext.getPlayer() == null) return super.useOnBlock(useOnContext);
        PlayerEntity player = useOnContext.getPlayer();
        BlockPos blockPos = useOnContext.getBlockPos();
        World level = useOnContext.getWorld();
        if (RespawnObelisksConfig.INSTANCE.teleportation.allowedBindingBlocks.contains(level.getBlockState(blockPos))) {
            level.playSound(null, blockPos, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0f, 1.0f);
            ItemStack itemStack = useOnContext.getStack();
            if (!player.getAbilities().creativeMode && itemStack.getCount() == 1) {
                this.writeNbt(level.getRegistryKey(), blockPos, itemStack.getOrCreateNbt());
            } else {
                ItemStack itemStack2 = new ItemStack(ModRegistries.boundCompass.get(), 1);
                NbtCompound compoundTag = itemStack.hasNbt() ? itemStack.getNbt().copy() : new NbtCompound();
                itemStack2.setNbt(compoundTag);
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }
                this.writeNbt(level.getRegistryKey(), blockPos, compoundTag);
                if (!player.getInventory().insertStack(itemStack2)) {
                    player.dropItem(itemStack2, false);
                }
            }
            return ActionResult.success(level.isClient);
        } else if (useOnContext.getHand().equals(Hand.MAIN_HAND) && player instanceof ServerPlayerEntity serverPlayer && level instanceof ServerWorld serverLevel) {
            if (!RespawnObelisksConfig.INSTANCE.teleportation.allowCursedTeleportation && player.hasStatusEffect(ModRegistries.immortalityCurse.get())) {
                serverPlayer.sendMessageToClient(Text.translatable("text.respawnobelisks.wormhole_cursed"), true);
                return ActionResult.FAIL;
            }
            GlobalPos pos = createLodestonePos(player.getMainHandStack().getOrCreateNbt());
            if (
                pos != null &&
                level.getBlockState(pos.getPos().up()).getBlock() instanceof RespawnObeliskBlock block &&
                level.getBlockEntity(pos.getPos().up()) instanceof RespawnObeliskBlockEntity blockEntity &&
                blockEntity.getCharge(player) >= RespawnObelisksConfig.INSTANCE.teleportation.minimumTpRadiance
            ) {
                if (!CoreUtils.hasInteraction(blockEntity.getCoreInstance(), ObeliskInteraction.TELEPORT)) {
                    serverPlayer.sendMessageToClient(Text.translatable("text.respawnobelisks.wormhole_invalid"), true);
                    return ActionResult.FAIL;
                }
                if (PlayerUtil.getTotalXp(player) < RespawnObelisksConfig.INSTANCE.teleportation.xpCost || player.experienceLevel < RespawnObelisksConfig.INSTANCE.teleportation.levelCost) {
                    serverPlayer.sendMessageToClient(Text.translatable("text.respawnobelisks.wormhole_failed_requirements"), true);
                    return ActionResult.FAIL;
                }
                BlockState state = level.getBlockState(pos.getPos().up());
                Optional<Vec3d> obeliskLoc = block.getRespawnLocation(true, false, false, state, pos.getPos().up(), serverLevel, serverPlayer);
                obeliskLoc.ifPresent(vec3 -> {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 130, 0, true, false));
                    RuneCircles.getCache(serverLevel).create(serverPlayer, serverPlayer.getMainHandStack(), pos.getPos().up(), new BlockPos((int) vec3.x, (int) vec3.y, (int) vec3.z), serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
                });
                return ActionResult.SUCCESS;
            }
            serverPlayer.sendMessageToClient(Text.translatable("text.respawnobelisks.wormhole_invalid"), true);
            return ActionResult.FAIL;
        }
        return super.useOnBlock(useOnContext);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient) return;
        if (hasLodestone(stack)) {
            BlockPos blockPos;
            NbtCompound nbtCompound = stack.getOrCreateNbt();
            if (nbtCompound.contains(LODESTONE_TRACKED_KEY) && !nbtCompound.getBoolean(LODESTONE_TRACKED_KEY)) {
                return;
            }
            Optional<RegistryKey<World>> optional = getLodestoneDimension(nbtCompound);
            if (optional.isPresent() && optional.get() == world.getRegistryKey() && nbtCompound.contains(LODESTONE_POS_KEY) && (!world.isInBuildLimit(blockPos = NbtHelper.toBlockPos(nbtCompound.getCompound(LODESTONE_POS_KEY))) || !RespawnObelisksConfig.INSTANCE.teleportation.allowedBindingBlocks.contains(world.getBlockState(blockPos))))
                nbtCompound.remove(LODESTONE_POS_KEY);
        }
    }

    @Override
    public void appendTooltip(ItemStack itemStack, @Nullable World level, List<Text> list, TooltipContext tooltipFlag) {
        if (level != null && level.isClient) ClientUtils.addCompassTooltipLines(itemStack, level, list, tooltipFlag);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return ClientUtils.isBoundCompassBarVisible(stack);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return MathHelper.hsvToRgb(0.5f, 0.66f, 1f);
    }

    @Override
    public int getItemBarStep(ItemStack itemStack) {
        return ClientUtils.getBoundCompassBarWidth(itemStack);
    }

    @Override
    public String getTranslationKey(ItemStack itemStack) {
        return hasLodestone(itemStack) ? "item.respawnobelisks.bound_bound_compass" : super.getTranslationKey(itemStack);
    }

    private void writeNbt(RegistryKey<World> resourceKey, BlockPos blockPos, NbtCompound compoundTag) {
        compoundTag.put(LODESTONE_POS_KEY, NbtHelper.fromBlockPos(blockPos));
        World.CODEC.encodeStart(NbtOps.INSTANCE, resourceKey).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put(LODESTONE_DIMENSION_KEY, (NbtElement)tag));
        compoundTag.putBoolean(LODESTONE_TRACKED_KEY, true);
    }

    protected static Optional<RegistryKey<World>> getLodestoneDimension(NbtCompound nbt) {
        return World.CODEC.parse(NbtOps.INSTANCE, nbt.get(LODESTONE_DIMENSION_KEY)).result();
    }
}
