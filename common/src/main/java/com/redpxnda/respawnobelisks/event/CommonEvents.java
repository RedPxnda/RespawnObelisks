package com.redpxnda.respawnobelisks.event;

import com.redpxnda.respawnobelisks.config.*;
import com.redpxnda.respawnobelisks.data.saved.AnchorExplosions;
import com.redpxnda.respawnobelisks.data.saved.RuneCircles;
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
import dev.architectury.platform.Platform;
import dev.architectury.utils.value.IntValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CommonEvents {
    public static EventResult onBlockInteract(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if (!hand.equals(InteractionHand.MAIN_HAND) || !player.getMainHandItem().is(Items.RECOVERY_COMPASS) || !(player.getLevel().getBlockState(pos).is(Blocks.LODESTONE))) return EventResult.pass();
        if (TeleportConfig.enableTeleportation) player.setItemInHand(hand, new ItemStack(ModRegistries.BOUND_COMPASS.get()));
        BlockHitResult hitResult = new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), face, pos, false);
        if (player.getItemInHand(hand).getItem() instanceof BoundCompassItem item) item.useOn(new UseOnContext(player, hand, hitResult));
        return EventResult.pass();
    }

    public static EventResult onBreakBlock(Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
        if (state.getBlock() instanceof RespawnObeliskBlock) {
            if (state.getValue(RespawnObeliskBlock.HALF).equals(DoubleBlockHalf.UPPER))
                pos = pos.below();
            if (
                    level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity && ( // making sure the block is a respawn obelisk block (entity)
                            (!TrustedPlayersConfig.allowObeliskBreaking && !blockEntity.isPlayerTrusted(player.getScoreboardName())) || // if untrusted
                            (blockEntity.hasStoredItems && !player.isShiftKeyDown()) || // if has items inside
                            (!blockEntity.getItemStack().isEmpty() && !player.isShiftKeyDown()) || // if has core inside
                            (blockEntity.hasTeleportingEntity) // if has teleporting entity
                    )
            )
                return EventResult.interruptFalse(); // prevent block break
        }
        return EventResult.pass();
    }

    public static EventResult onEntityInteract(Player player, Entity entity, InteractionHand hand) {
        if (player.level.isClientSide || !hand.equals(InteractionHand.MAIN_HAND) || player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem()) || !player.getMainHandItem().hasTag() || !player.getMainHandItem().getTag().contains("RespawnObeliskData")) return EventResult.pass();

        if (ReviveConfig.enableRevival && CoreUtils.hasCapability(player.getMainHandItem(), CoreUtils.Capability.REVIVE)) {
            if (!(entity instanceof Player) && entity instanceof LivingEntity && ReviveConfig.isEntityListed(entity)) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.getTag().getCompound("RespawnObeliskData").contains("SavedEntities"))
                    stack.getTag().getCompound("RespawnObeliskData").put("SavedEntities", new ListTag());
                ListTag listTag = stack.getTag().getCompound("RespawnObeliskData").getList("SavedEntities", 10);
                if (listTag.size() >= ObeliskCoreConfig.maxStoredEntities) return EventResult.pass();
                if (!containsUUID(listTag, entity.getUUID())) {
                    CompoundTag entityTag = new CompoundTag();

                    entityTag.putUUID("uuid", entity.getUUID());
                    entityTag.putString("type", Registry.ENTITY_TYPE.getKey(entity.getType()).toString()); // putting the type in
                    entityTag.put("data", new CompoundTag()); // data initialization
                    entityTag.getCompound("data").putString("DeathLootTable", "minecraft:empty");
                    entity.saveWithoutId(entityTag.getCompound("data")); // putting data in

                    if (!listTag.contains(entityTag)) {
                        listTag.add(entityTag); // add entity to item nbt
                        player.getCooldowns().addCooldown(stack.getItem(), 100); // add item cooldown
                        return EventResult.interruptFalse();
                    }
                } else {
                    removeUUID(listTag, entity.getUUID());
                    player.getCooldowns().addCooldown(stack.getItem(), 100);
                }
            }
        }
        if (TrustedPlayersConfig.enablePlayerTrust && entity instanceof Player interacted && CoreUtils.hasCapability(player.getMainHandItem(), CoreUtils.Capability.PROTECT)) {
            ItemStack stack = player.getMainHandItem();
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
        if (Platform.isFabric()) { // architectury messed up params on fabric, i have to switch em myself until arch fixes.
            ServerPlayer temp = oldPlayer;
            oldPlayer = newPlayer;
            newPlayer = temp;
        }
        if (wonGame) return;
        if (oldPlayer.hasEffect(ModRegistries.IMMORTALITY_CURSE.get())) cloneAddCurse(newPlayer, oldPlayer);
        if (
            oldPlayer.getRespawnPosition() != null &&
            oldPlayer.level.getBlockEntity(oldPlayer.getRespawnPosition()) instanceof RespawnObeliskBlockEntity be
        ) {
            be.restoreSavedItems(newPlayer);
        }
    }

    private static void cloneAddCurse(ServerPlayer newPlayer, ServerPlayer oldPlayer) {
        MobEffectInstance MEI = oldPlayer.getEffect(ModRegistries.IMMORTALITY_CURSE.get());
        if (MEI == null) return;
        int amplifier = MEI.getAmplifier();
        if (amplifier == CurseConfig.curseMaxLevel+1) amplifier = -1;
        amplifier = Math.min(amplifier+CurseConfig.curseLevelIncrement, CurseConfig.curseMaxLevel-1);
        newPlayer.addEffect(new MobEffectInstance(MEI.getEffect(), CurseConfig.curseDuration, amplifier));
    }

    public static void onPlayerRespawn(ServerPlayer player, boolean conqueredEnd) {
        if (player.hasEffect(ModRegistries.IMMORTALITY_CURSE.get())) {
            MobEffectInstance MEI = player.getEffect(ModRegistries.IMMORTALITY_CURSE.get());
            if (MEI == null) return;
            ModPackets.CHANNEL.sendToPlayer(player, new SyncEffectsPacket(MEI.getAmplifier(), MEI.getDuration()));
        }
    }

    public static void onServerTick(ServerLevel level) {
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
