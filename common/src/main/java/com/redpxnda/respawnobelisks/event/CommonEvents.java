package com.redpxnda.respawnobelisks.event;

import com.redpxnda.respawnobelisks.config.ServerConfig;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.SyncEffectsPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.structure.VillageAddition;
import com.redpxnda.respawnobelisks.scheduled.ScheduledTasks;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CommonEvents {
    public static CompoundEventResult<ItemStack> onItemClick(Player player, InteractionHand hand) {
        if (player.level.isClientSide || !hand.equals(InteractionHand.MAIN_HAND) || player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem()) || !player.getMainHandItem().hasTag() || !player.getMainHandItem().getTag().contains("RespawnObeliskData")) return CompoundEventResult.pass();

        ItemStack stack = player.getMainHandItem();
        if (!stack.getTag().getCompound("RespawnObeliskData").contains("SavedEntities"))
            stack.getTag().getCompound("RespawnObeliskData").put("SavedEntities", new ListTag());
        ListTag listTag = stack.getTag().getCompound("RespawnObeliskData").getList("SavedEntities", 10);

        if (player.isShiftKeyDown()) {
            if (listTag.isEmpty()) return CompoundEventResult.pass();
            listTag.forEach(tag -> {
                if (
                        tag instanceof CompoundTag compound &&
                                compound.contains("uuid") &&
                                compound.contains("type") &&
                                compound.contains("data")
                ) {
                    if (player.level instanceof ServerLevel serverLevel) {
                        Entity entity = serverLevel.getEntity(compound.getUUID("uuid"));
                        if (entity != null && entity.isAlive()) {
                            return;
                        }
                    }
                    Entity toSummon = Registry.ENTITY_TYPE.get(ResourceLocation.tryParse(compound.getString("type"))).create(player.level);
                    if (toSummon == null) return;
                    toSummon.load(compound.getCompound("data"));
                    toSummon.setPos(player.getX(), player.getY(), player.getZ());
                    player.level.addFreshEntity(toSummon);
                }
            });
            player.getCooldowns().addCooldown(stack.getItem(), 100); // add item cooldown
            return CompoundEventResult.interruptFalse(player.getMainHandItem());
        }
        return CompoundEventResult.pass();
    }

    public static EventResult onEntityInteract(Player player, Entity entity, InteractionHand hand) {
        if (player.level.isClientSide || !hand.equals(InteractionHand.MAIN_HAND) || player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem()) || !player.getMainHandItem().hasTag() || !player.getMainHandItem().getTag().contains("RespawnObeliskData")) return EventResult.pass();
        if (!(entity instanceof Player)) {
            ItemStack stack = player.getMainHandItem();
            if (!stack.getTag().getCompound("RespawnObeliskData").contains("SavedEntities"))
                stack.getTag().getCompound("RespawnObeliskData").put("SavedEntities", new ListTag());
            ListTag listTag = stack.getTag().getCompound("RespawnObeliskData").getList("SavedEntities", 10);
            CompoundTag entityTag = new CompoundTag();

            entityTag.putUUID("uuid", entity.getUUID());
            entityTag.putString("type", Registry.ENTITY_TYPE.getKey(entity.getType()).toString()); // putting the type in
            entityTag.put("data", new CompoundTag()); // data initialization
            entity.saveWithoutId(entityTag.getCompound("data")); // putting data in

            if (!listTag.contains(entityTag)) {
                listTag.add(entityTag); // add entity to item nbt
                player.getCooldowns().addCooldown(stack.getItem(), 100); // add item cooldown
                return EventResult.interruptFalse();
            }
        }
        return EventResult.pass();
    }

    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wonGame) {
        if (Platform.isFabric()) { // architectury messed up params on fabric, i have to switch em myself until arch fixes.
            ServerPlayer temp = oldPlayer;
            oldPlayer = newPlayer;
            newPlayer = temp;
        }
        if (wonGame) return;
        if (!oldPlayer.hasEffect(ModRegistries.IMMORTALITY_CURSE.get())) return;
        MobEffectInstance MEI = oldPlayer.getEffect(ModRegistries.IMMORTALITY_CURSE.get());
        if (MEI == null) return;
        int amplifier = MEI.getAmplifier();
        if (amplifier == 0) amplifier--;
        for (int i = 0; i < ServerConfig.curseLevelIncrement; i++) {
            if (ServerConfig.curseMaxLevel-1 > amplifier) amplifier++;
            else break;
        }
        newPlayer.addEffect(new MobEffectInstance(MEI.getEffect(), ServerConfig.curseDuration, amplifier));
    }

    public static void onPlayerRespawn(ServerPlayer player, boolean conqueredEnd) {
        if (player.hasEffect(ModRegistries.IMMORTALITY_CURSE.get())) {
            MobEffectInstance MEI = player.getEffect(ModRegistries.IMMORTALITY_CURSE.get());
            if (MEI == null) return;
            ModPackets.CHANNEL.sendToPlayer(player, new SyncEffectsPacket(MEI.getAmplifier(), MEI.getDuration()));
        }
    }

    public static void init() {
        LifecycleEvent.SERVER_BEFORE_START.register(VillageAddition::addNewVillageBuilding);
        TickEvent.SERVER_POST.register(ScheduledTasks::onServerTick);
        PlayerEvent.PLAYER_CLONE.register(CommonEvents::onPlayerClone);
        PlayerEvent.PLAYER_RESPAWN.register(CommonEvents::onPlayerRespawn);
        InteractionEvent.INTERACT_ENTITY.register(CommonEvents::onEntityInteract);
        InteractionEvent.RIGHT_CLICK_ITEM.register(CommonEvents::onItemClick);
    }
}
