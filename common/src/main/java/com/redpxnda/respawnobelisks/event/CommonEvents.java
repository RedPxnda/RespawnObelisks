package com.redpxnda.respawnobelisks.event;

import com.redpxnda.respawnobelisks.config.*;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.SyncEffectsPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.structure.VillageAddition;
import com.redpxnda.respawnobelisks.scheduled.server.ScheduledServerTasks;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import dev.architectury.platform.Platform;
import dev.architectury.utils.value.IntValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CommonEvents {
    public static EventResult onBreakBlock(Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
        if (state.getBlock() instanceof RespawnObeliskBlock) {
            if (state.getValue(RespawnObeliskBlock.HALF).equals(DoubleBlockHalf.UPPER))
                pos = pos.below();
            if (!TrustedPlayersConfig.allowObeliskBreaking && level.getBlockEntity(pos) instanceof RespawnObeliskBlockEntity blockEntity && !blockEntity.isPlayerTrusted(player.getScoreboardName()))
                return EventResult.interruptFalse();
        }
        return EventResult.pass();
    }

    public static EventResult onEntityInteract(Player player, Entity entity, InteractionHand hand) {
        if (player.level.isClientSide || !hand.equals(InteractionHand.MAIN_HAND) || player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem()) || !player.getMainHandItem().hasTag() || !player.getMainHandItem().getTag().contains("RespawnObeliskData")) return EventResult.pass();

        if (ReviveConfig.enableRevival) {
            List<String> entities = Arrays.asList(ReviveConfig.revivableEntities);
            boolean bl =
                    (entities.contains("$tamables") && entity instanceof OwnableEntity) ||
                    (entities.contains("$animals") && entity instanceof Animal) ||
                    (entities.contains("$merchants") && entity instanceof Merchant) ||
                    (entities.contains(Registry.ENTITY_TYPE.getKey(entity.getType()).toString()));
            for (String str : entities) {
                if (!str.startsWith("#")) continue;
                str = str.substring(1);
                ResourceLocation loc = ResourceLocation.tryParse(str);
                if (loc == null) continue;
                var tag = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, loc);
                if (entity.getType().is(tag)) {
                    bl = true;
                    break;
                }
            }

            if (ReviveConfig.entitiesIsBlacklist) bl = !bl;

            if (!(entity instanceof Player) && entity instanceof LivingEntity && bl) {
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
                }
            }
        }
        if (TrustedPlayersConfig.enablePlayerTrust && entity instanceof Player interacted) {
            ItemStack stack = player.getMainHandItem();
            if (!stack.getTag().getCompound("RespawnObeliskData").contains("TrustedPlayers"))
                stack.getTag().getCompound("RespawnObeliskData").put("TrustedPlayers", new ListTag());
            ListTag listTag = stack.getTag().getCompound("RespawnObeliskData").getList("TrustedPlayers", 8);

            if (!listTag.contains(StringTag.valueOf(interacted.getScoreboardName()))) {
                listTag.add(StringTag.valueOf("")); // add entity to item nbt
                player.getCooldowns().addCooldown(stack.getItem(), 100); // add item cooldown
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
        for (int i = 0; i < CurseConfig.curseLevelIncrement; i++) {
            if (CurseConfig.curseMaxLevel-1 > amplifier) amplifier++;
            else break;
        }
        newPlayer.addEffect(new MobEffectInstance(MEI.getEffect(), CurseConfig.curseDuration, amplifier));
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
        TickEvent.SERVER_POST.register(ScheduledServerTasks::onServerTick);
        PlayerEvent.PLAYER_CLONE.register(CommonEvents::onPlayerClone);
        PlayerEvent.PLAYER_RESPAWN.register(CommonEvents::onPlayerRespawn);
        InteractionEvent.INTERACT_ENTITY.register(CommonEvents::onEntityInteract);
        BlockEvent.BREAK.register(CommonEvents::onBreakBlock);
    }
}
