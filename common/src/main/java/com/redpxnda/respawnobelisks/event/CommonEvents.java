package com.redpxnda.respawnobelisks.event;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import com.redpxnda.respawnobelisks.data.listener.RevivedNbtEditing;
import com.redpxnda.respawnobelisks.data.saved.AnchorExplosions;
import com.redpxnda.respawnobelisks.data.saved.RuneCircles;
import com.redpxnda.respawnobelisks.facet.HardcoreRespawningTracker;
import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.SyncEffectsPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.item.BoundCompassItem;
import com.redpxnda.respawnobelisks.registry.structure.VillageAddition;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import dev.architectury.utils.value.IntValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CommonEvents {
    public static EventResult onBlockInteract(PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
        if (!hand.equals(Hand.MAIN_HAND) || !player.getMainHandStack().isOf(Items.RECOVERY_COMPASS) || !RespawnObelisksConfig.INSTANCE.teleportation.allowedBindingBlocks.contains(player.getWorld().getBlockState(pos))) return EventResult.pass();
        if (RespawnObelisksConfig.INSTANCE.teleportation.enableTeleportation) player.setStackInHand(hand, new ItemStack(ModRegistries.boundCompass.get()));
        BlockHitResult hitResult = new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), face, pos, false);
        if (player.getStackInHand(hand).getItem() instanceof BoundCompassItem item) item.useOnBlock(new ItemUsageContext(player, hand, hitResult));
        return EventResult.pass();
    }

    public static EventResult onBreakBlock(World level, BlockPos pos, BlockState state, ServerPlayerEntity player, @Nullable IntValue xp) {
        if (player.getAbilities().creativeMode) return EventResult.pass(); // if creative, skip
        if (state.getBlock() instanceof RespawnObeliskBlock) {
            if (state.get(RespawnObeliskBlock.HALF).equals(DoubleBlockHalf.UPPER))
                pos = pos.down();
            if (
                    level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity && ( // making sure the block is a respawn obelisk block (entity)
                            (!RespawnObelisksConfig.INSTANCE.playerTrusting.allowObeliskBreaking && !blockEntity.isPlayerTrusted(player.getEntityName())) || // if untrusted
                            (blockEntity.hasStoredItems && !player.isSneaking()) || // if has items inside
                            (!blockEntity.getItemStack().isEmpty() && !player.isSneaking()) || // if has core inside
                            (blockEntity.hasTeleportingEntity) // if has teleporting entity
                    )
            )
                return EventResult.interruptFalse(); // prevent block break
        }
        return EventResult.pass();
    }

    public static EventResult onEntityInteract(PlayerEntity player, Entity entity, Hand hand) {
        Identifier rl;
        if (player.getWorld().isClient || !hand.equals(Hand.MAIN_HAND) || !ObeliskCore.CORES.containsKey(rl = Registries.ITEM.getId(player.getMainHandStack().getItem())) || player.getItemCooldownManager().isCoolingDown(player.getMainHandStack().getItem())) return EventResult.pass();
        ObeliskCore.Instance core = new ObeliskCore.Instance(player.getMainHandStack(), ObeliskCore.CORES.get(rl));
        ItemStack stack = core.stack();
        if (!stack.getOrCreateNbt().contains("RespawnObeliskData"))
            stack.getNbt().put("RespawnObeliskData", new NbtCompound());

        if (RespawnObelisksConfig.INSTANCE.revival.enableRevival && CoreUtils.hasInteraction(core, ObeliskInteraction.REVIVE)) {
            if (!(entity instanceof PlayerEntity) && entity instanceof LivingEntity && RespawnObelisksConfig.INSTANCE.revival.isEntityListed(entity)) {
                if (!stack.getNbt().getCompound("RespawnObeliskData").contains("SavedEntities"))
                    stack.getNbt().getCompound("RespawnObeliskData").put("SavedEntities", new NbtList());
                NbtList listTag = stack.getNbt().getCompound("RespawnObeliskData").getList("SavedEntities", 10);
                if (listTag.size() >= RespawnObelisksConfig.INSTANCE.cores.maxStoredEntities) return EventResult.pass();
                if (!containsUUID(listTag, entity.getUuid())) {
                    NbtCompound entityTag = new NbtCompound();

                    entityTag.putUuid("uuid", entity.getUuid());
                    entityTag.putString("type", Registries.ENTITY_TYPE.getId(entity.getType()).toString());
                    NbtCompound dataTag = new NbtCompound();
                    entity.writeNbt(dataTag); // filling data info
                    RevivedNbtEditing.modify(dataTag, entity);
                    entityTag.put("data", dataTag);

                    if (!listTag.contains(entityTag)) {
                        listTag.add(entityTag); // add entity to item nbt
                        player.getItemCooldownManager().set(stack.getItem(), 50); // add item cooldown
                        player.sendMessage(
                                Text.translatable("text.respawnobelisks.revive_mob_warning")
                                .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.translatable("text.respawnobelisks.revive_mob_warning.hover")))));
                        return EventResult.interruptFalse();
                    }
                } else {
                    removeUUID(listTag, entity.getUuid());
                    player.getItemCooldownManager().set(stack.getItem(), 50);
                }
            }
        }
        if (RespawnObelisksConfig.INSTANCE.playerTrusting.enablePlayerTrust && entity instanceof PlayerEntity interacted && CoreUtils.hasInteraction(core, ObeliskInteraction.PROTECT)) {
            if (!stack.getNbt().getCompound("RespawnObeliskData").contains("TrustedPlayers"))
                stack.getNbt().getCompound("RespawnObeliskData").put("TrustedPlayers", new NbtList());
            NbtList listTag = stack.getNbt().getCompound("RespawnObeliskData").getList("TrustedPlayers", 8);

            if (!listTag.contains(NbtString.of(interacted.getEntityName()))) {
                listTag.add(NbtString.of(interacted.getEntityName())); // add entity to item nbt
                if (!listTag.contains(NbtString.of(player.getEntityName()))) listTag.add(NbtString.of(player.getEntityName()));
                player.getItemCooldownManager().set(stack.getItem(), 100); // add item cooldown
                return EventResult.interruptFalse();
            } else {
                listTag.remove(NbtString.of(interacted.getEntityName()));
                if (!listTag.contains(NbtString.of(player.getEntityName()))) listTag.add(NbtString.of(player.getEntityName()));
                player.getItemCooldownManager().set(stack.getItem(), 100);
                return EventResult.interruptFalse();
            }
        }
        return EventResult.pass();
    }

    private static boolean containsUUID(NbtList tag, UUID uuid) {
        for (NbtElement value : tag)
            if (value instanceof NbtCompound compound) {
                if (compound.getUuid("uuid").equals(uuid))
                    return true;
            }
        return false;
    }

    private static void removeUUID(NbtList tag, UUID uuid) {
        tag.removeIf(t -> t instanceof NbtCompound compound && compound.getUuid("uuid").equals(uuid));
    }

    public static void onPlayerClone(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean wonGame) {
        if (RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.enableSecondarySpawnPoints) {
            SecondarySpawnPoints oldFacet = SecondarySpawnPoints.KEY.get(oldPlayer);
            SecondarySpawnPoints newFacet = SecondarySpawnPoints.KEY.get(newPlayer);
            if (oldFacet != null && newFacet != null) {
                newFacet.points.clear();
                newFacet.points.addAll(oldFacet.points);
            }
        }

        if (RespawnObelisksConfig.INSTANCE.allowHardcoreRespawning) {
            HardcoreRespawningTracker oldFacet = HardcoreRespawningTracker.KEY.get(oldPlayer);
            HardcoreRespawningTracker newFacet = HardcoreRespawningTracker.KEY.get(newPlayer);
            if (oldFacet != null && newFacet != null) newFacet.canRespawn = oldFacet.canRespawn;
        }

        if (wonGame) return;
        if (oldPlayer.hasStatusEffect(ModRegistries.immortalityCurse.get())) cloneAddCurse(newPlayer, oldPlayer);
        if (
            oldPlayer.getSpawnPointPosition() != null &&
            oldPlayer.getWorld().getBlockEntity(oldPlayer.getSpawnPointPosition()) instanceof RespawnObeliskBlockEntity be
        ) {
            be.restoreSavedItems(newPlayer);
        }
    }

    private static void cloneAddCurse(ServerPlayerEntity newPlayer, ServerPlayerEntity oldPlayer) {
        StatusEffectInstance MEI = oldPlayer.getStatusEffect(ModRegistries.immortalityCurse.get());
        if (MEI == null) return;
        int amplifier = MEI.getAmplifier();
        if (amplifier == RespawnObelisksConfig.INSTANCE.immortalityCurse.curseMaxLevel+1) amplifier = -1;
        amplifier = Math.min(amplifier+RespawnObelisksConfig.INSTANCE.immortalityCurse.curseLevelIncrement, RespawnObelisksConfig.INSTANCE.immortalityCurse.curseMaxLevel-1);
        newPlayer.addStatusEffect(new StatusEffectInstance(MEI.getEffectType(), RespawnObelisksConfig.INSTANCE.immortalityCurse.curseDuration, amplifier));
    }

    public static void onPlayerRespawn(ServerPlayerEntity player, boolean conqueredEnd) {
        if (player.hasStatusEffect(ModRegistries.immortalityCurse.get())) {
            StatusEffectInstance MEI = player.getStatusEffect(ModRegistries.immortalityCurse.get());
            if (MEI == null) return;
            ModPackets.CHANNEL.sendToPlayer(player, new SyncEffectsPacket(MEI.getAmplifier(), MEI.getDuration()));
        }
    }

    public static void onServerTick(ServerWorld level) {
        RuneCircles.getCache(level).tick();
        AnchorExplosions.getCache(level).tick();
    }

    public static void init() {
        LifecycleEvent.SERVER_BEFORE_START.register(VillageAddition::addNewVillageBuilding);
        TickEvent.SERVER_LEVEL_POST.register(CommonEvents::onServerTick);
//        LifecycleEvent.SERVER_STOPPING.register(ScheduledServerTasks::onServerStop);
//        LifecycleEvent.SERVER_STARTING.register(ScheduledServerTasks::onServerStart);
//        TickEvent.SERVER_POST.register(ScheduledServerTasks::onServerTick);
        PlayerEvent.PLAYER_CLONE.register(CommonEvents::onPlayerClone);
        PlayerEvent.PLAYER_RESPAWN.register(CommonEvents::onPlayerRespawn);
        InteractionEvent.INTERACT_ENTITY.register(CommonEvents::onEntityInteract);
        InteractionEvent.RIGHT_CLICK_BLOCK.register(CommonEvents::onBlockInteract);
        BlockEvent.BREAK.register(CommonEvents::onBreakBlock);
    }
}
