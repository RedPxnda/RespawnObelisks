package com.redpxnda.respawnobelisks.event;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import com.redpxnda.respawnobelisks.data.listener.RevivedNbtEditing;
import com.redpxnda.respawnobelisks.data.saved.AnchorExplosions;
import com.redpxnda.respawnobelisks.data.saved.LimboEntities;
import com.redpxnda.respawnobelisks.data.saved.RuneCircles;
import com.redpxnda.respawnobelisks.facet.HardcoreRespawningTracker;
import com.redpxnda.respawnobelisks.facet.LimboReviveTracker;
import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import com.redpxnda.respawnobelisks.facet.kept.KeptRespawnItems;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.SyncEffectsPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.item.BoundCompassItem;
import com.redpxnda.respawnobelisks.registry.structure.VillageAddition;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import dev.architectury.utils.value.IntValue;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CommonEvents {
    public static EventResult onBlockInteract(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if (RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.allowPriorityShifting && player instanceof ServerPlayer sp && player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.enableSecondarySpawnPoints) {
            SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(player);
            if (facet != null) {
                SpawnPoint point = new SpawnPoint(player.level().dimension(), pos, 0, false);
                if (facet.points.contains(point)) {
                    if (facet.reorderingTarget == null) {
                        facet.reorderingTarget = point;
                        facet.sendToClient(sp);
                        return EventResult.interruptFalse();
                    } else {
                        facet.reorderingTarget = null;
                        facet.sendToClient(sp);
                    }
                }
            }
        }

        if (!hand.equals(InteractionHand.MAIN_HAND) || !player.getMainHandItem().is(Items.RECOVERY_COMPASS) || RespawnObelisksConfig.INSTANCE.teleportation.getBlockBindPosition(player.level(), pos) == null) return EventResult.pass();
        if (RespawnObelisksConfig.INSTANCE.teleportation.enableTeleportation) {
            ItemStack stack = player.getItemInHand(hand);
            player.setItemInHand(hand, new ItemStack(ModRegistries.boundCompass.get()));
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.getInventory().placeItemBackInInventory(stack);
        }
        BlockHitResult hitResult = new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), face, pos, false);
        if (player.getItemInHand(hand).getItem() instanceof BoundCompassItem item) item.useOn(new UseOnContext(player, hand, hitResult));
        return EventResult.pass();
    }

    public static EventResult onBreakBlock(Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
        if (player.getAbilities().instabuild) return EventResult.pass(); // if creative, skip
        if (state.getBlock() instanceof RespawnObeliskBlock) {
            if (state.getValue(RespawnObeliskBlock.HALF).equals(DoubleBlockHalf.UPPER))
                pos = pos.below();
            if (
                    level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity && ( // making sure the block is a respawn obelisk block (entity)
                            (!RespawnObelisksConfig.INSTANCE.playerTrusting.allowObeliskBreaking && !blockEntity.isPlayerTrusted(player.getScoreboardName())) || // if untrusted
                            (!blockEntity.getItemStack().isEmpty() && !player.isShiftKeyDown()) || // if has core inside
                            (blockEntity.hasTeleportingEntity) // if has teleporting entity
                    )
            )
                return EventResult.interruptFalse(); // prevent block break
        }
        return EventResult.pass();
    }

    public static EventResult onEntityInteract(Player player, Entity entity, InteractionHand hand) {
        ResourceLocation rl;
        if (player.level().isClientSide || !hand.equals(InteractionHand.MAIN_HAND) || !ObeliskCore.CORES.containsKey(rl = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem())) || player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())) return EventResult.pass();
        ObeliskCore.Instance core = new ObeliskCore.Instance(player.getMainHandItem(), ObeliskCore.CORES.get(rl));
        ItemStack stack = core.stack();
        if (!stack.getOrCreateTag().contains("RespawnObeliskData"))
            stack.getTag().put("RespawnObeliskData", new CompoundTag());

        if (RespawnObelisksConfig.INSTANCE.revival.enableRevival && CoreUtils.hasInteraction(core, ObeliskInteraction.REVIVE)) {
            if (!(entity instanceof Player) && entity instanceof LivingEntity && RespawnObelisksConfig.INSTANCE.revival.isEntityListed(entity)) {
                if (!stack.getTag().getCompound("RespawnObeliskData").contains("SavedEntities"))
                    stack.getTag().getCompound("RespawnObeliskData").put("SavedEntities", new ListTag());
                ListTag listTag = stack.getTag().getCompound("RespawnObeliskData").getList("SavedEntities", 10);
                if (listTag.size() >= RespawnObelisksConfig.INSTANCE.cores.maxStoredEntities) return EventResult.pass();
                LimboReviveTracker tracker = LimboReviveTracker.KEY.get(entity);
                if (tracker != null) {
                    if (!containsUUID(listTag, entity.getUUID())) {
                        tracker.trackers++;
                        CompoundTag entityTag = new CompoundTag();

                        entityTag.putUUID("uuid", entity.getUUID());
                        entityTag.putString("type", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
                        CompoundTag dataTag = new CompoundTag();
                        entity.saveWithoutId(dataTag); // filling data info
                        RevivedNbtEditing.modify(dataTag, entity);
                        entityTag.put("data", dataTag);

                        if (!listTag.contains(entityTag)) {
                            listTag.add(entityTag); // add entity to item nbt
                            player.getCooldowns().addCooldown(stack.getItem(), 50); // add item cooldown
                            player.sendSystemMessage(
                                    Component.translatable("text.respawnobelisks.revive_mob_warning")
                                            .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Component.translatable("text.respawnobelisks.revive_mob_warning.hover")))));
                            return EventResult.interruptFalse();
                        }
                    } else {
                        tracker.trackers--;
                        removeUUID(listTag, entity.getUUID());
                        player.getCooldowns().addCooldown(stack.getItem(), 50);
                    }
                }
            }
        }
        if (RespawnObelisksConfig.INSTANCE.playerTrusting.enablePlayerTrust && entity instanceof Player interacted && CoreUtils.hasInteraction(core, ObeliskInteraction.PROTECT)) {
            if (!stack.getTag().getCompound("RespawnObeliskData").contains("TrustedPlayers"))
                stack.getTag().getCompound("RespawnObeliskData").put("TrustedPlayers", new ListTag());
            ListTag listTag = stack.getTag().getCompound("RespawnObeliskData").getList("TrustedPlayers", 8);

            if (!listTag.contains(StringTag.valueOf(interacted.getScoreboardName()))) {
                listTag.add(StringTag.valueOf(interacted.getScoreboardName())); // add entity to item nbt
                if (!listTag.contains(StringTag.valueOf(player.getScoreboardName()))) listTag.add(StringTag.valueOf(player.getScoreboardName()));
                player.getCooldowns().addCooldown(stack.getItem(), 100); // add item cooldown
                return EventResult.interruptFalse();
            } else {
                listTag.remove(StringTag.valueOf(interacted.getScoreboardName()));
                if (!listTag.contains(StringTag.valueOf(player.getScoreboardName()))) listTag.add(StringTag.valueOf(player.getScoreboardName()));
                player.getCooldowns().addCooldown(stack.getItem(), 100);
                return EventResult.interruptFalse();
            }
        }
        return EventResult.pass();
    }

    private static boolean containsUUID(ListTag tag, UUID uuid) {
        for (Tag value : tag)
            if (value instanceof CompoundTag compound) {
                if (compound.getUUID("uuid").equals(uuid))
                    return true;
            }
        return false;
    }

    private static void removeUUID(ListTag tag, UUID uuid) {
        tag.removeIf(t -> t instanceof CompoundTag compound && compound.getUUID("uuid").equals(uuid));
    }

    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wonGame) {
        if (RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.enableSecondarySpawnPoints) {
            SecondarySpawnPoints oldFacet = SecondarySpawnPoints.KEY.get(oldPlayer);
            SecondarySpawnPoints newFacet = SecondarySpawnPoints.KEY.get(newPlayer);
            if (oldFacet != null && newFacet != null) {
                newFacet.points.clear();
                newFacet.points.addAll(oldFacet.points);
                if (RespawnObelisksConfig.INSTANCE.secondarySpawnPoints.enableBlockPriorities) newFacet.sortByPrio(newPlayer.getServer());
            }
        }

        if (RespawnObelisksConfig.INSTANCE.allowHardcoreRespawning) {
            HardcoreRespawningTracker oldFacet = HardcoreRespawningTracker.KEY.get(oldPlayer);
            HardcoreRespawningTracker newFacet = HardcoreRespawningTracker.KEY.get(newPlayer);
            if (oldFacet != null && newFacet != null) newFacet.canRespawn = oldFacet.canRespawn;
        }

        KeptRespawnItems oldFacet = KeptRespawnItems.KEY.get(oldPlayer);
        KeptRespawnItems newFacet = KeptRespawnItems.KEY.get(newPlayer);
        if (oldFacet != null && newFacet != null) newFacet.modules.putAll(oldFacet.modules);

        if (wonGame) return;
        if (oldPlayer.hasEffect(ModRegistries.immortalityCurse.get())) cloneAddCurse(newPlayer, oldPlayer);
        if (
            oldPlayer.getRespawnPosition() != null &&
            oldPlayer.level().getBlockEntity(oldPlayer.getRespawnPosition()) instanceof RespawnObeliskBlockEntity
        ) {
            ObeliskUtils.restoreSavedItems(oldPlayer, newPlayer);
        }// else ObeliskUtils.scatterSavedItems(oldPlayer);
    }

    private static void cloneAddCurse(ServerPlayer newPlayer, ServerPlayer oldPlayer) {
        MobEffectInstance MEI = oldPlayer.getEffect(ModRegistries.immortalityCurse.get());
        if (MEI == null) return;
        int amplifier = MEI.getAmplifier();
        if (amplifier == RespawnObelisksConfig.INSTANCE.immortalityCurse.curseMaxLevel+1) amplifier = -1;
        amplifier = Math.min(amplifier+RespawnObelisksConfig.INSTANCE.immortalityCurse.curseLevelIncrement, RespawnObelisksConfig.INSTANCE.immortalityCurse.curseMaxLevel-1);
        newPlayer.addEffect(new MobEffectInstance(MEI.getEffect(), RespawnObelisksConfig.INSTANCE.immortalityCurse.curseDuration, amplifier));
    }

    public static void onPlayerRespawn(ServerPlayer player, boolean conqueredEnd) {
        if (player.hasEffect(ModRegistries.immortalityCurse.get())) {
            MobEffectInstance MEI = player.getEffect(ModRegistries.immortalityCurse.get());
            if (MEI == null) return;
            ModPackets.CHANNEL.sendToPlayer(player, new SyncEffectsPacket(MEI.getAmplifier(), MEI.getDuration()));
        }
    }

    public static void onServerTick(ServerLevel level) {
        RuneCircles.getCache(level).tick();
        AnchorExplosions.getCache(level).tick();
    }

    public static EventResult onEntityDeath(LivingEntity entity, DamageSource source) {
        LimboReviveTracker tracker = LimboReviveTracker.KEY.get(entity);
        if (tracker != null) {
            int trackers = tracker.trackers;
            if (trackers > 0 && entity.getServer() != null) {
                ServerLevel overworld = entity.getServer().overworld();
                if (overworld != null) {
                    LimboEntities.getCache(overworld).limboEntities.put(entity.getUUID(), entity.saveWithoutId(new CompoundTag()));
                }
            }
        }
        return EventResult.pass();
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
        EntityEvent.LIVING_DEATH.register(CommonEvents::onEntityDeath); // todo AFTER death event
    }
}
