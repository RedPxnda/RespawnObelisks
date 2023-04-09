package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.respawnobelisks.config.RespawnPerkConfig;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.ObeliskInventory;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    private static Random random = new Random();

    @Inject(
            method = "dropAllDeathLoot",
            at = @At("HEAD")
    )
    private void RESPAWNOBELISKS_preventEquipmentDrop(DamageSource damageSource, CallbackInfo ci) {
        System.out.println("items are getting dropped!");
        if (
                (Object)this instanceof ServerPlayer player &&
                !player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) &&
                player.getRespawnPosition() != null &&
                player.level.getBlockEntity(player.getRespawnPosition()) instanceof RespawnObeliskBlockEntity be &&
                CoreUtils.hasCapability(be.getItemStack(), CoreUtils.Capability.SAVE_INV) &&
                be.getCharge(player) >= RespawnPerkConfig.minKeepItemCharge &&
                (RespawnPerkConfig.allowCursedItemKeeping || !player.hasEffect(ModRegistries.IMMORTALITY_CURSE.get()))
        ) {
            ObeliskInventory inventory = be.storedItems.containsKey(player.getUUID()) ? be.storedItems.get(player.getUUID()) : new ObeliskInventory();
            if (!player.wasExperienceConsumed() && RespawnPerkConfig.keepExperience && inventory.xp <= 0) {
                int rawXp = Mth.floor(player.experienceProgress * player.getXpNeededForNextLevel() + ObeliskUtils.getTotalXpForLevel(player.experienceLevel));
                inventory.xp = Mth.floor(rawXp*(RespawnPerkConfig.keepExperiencePercent/100f));
                if (RespawnPerkConfig.keepExperiencePercent >= 100) player.skipDropExperience();
                else player.giveExperiencePoints(-inventory.xp);
            }
            if (RespawnPerkConfig.Armor.keepArmor && inventory.isArmorEmpty()) {
                inventory.armor.clear();
                List<ItemStack> stacks = new ArrayList<>(
                        player.getInventory().armor.stream().map(i -> {
                            if (random.nextInt(100) <= RespawnPerkConfig.Armor.keepArmorChance-1) {
                                int index = player.getInventory().armor.indexOf(i);
                                player.getInventory().armor.set(index, ItemStack.EMPTY);
                                return i;
                            } else {
                                return ItemStack.EMPTY;
                            }
                        }).toList()
                );
                inventory.armor.addAll(stacks);
            }
            if (RespawnPerkConfig.Offhand.keepOffhand && !player.getOffhandItem().isEmpty() && inventory.isOffhandEmpty()) {
                inventory.offhand.clear();
                if (random.nextInt(100) <= RespawnPerkConfig.Offhand.keepOffhandChance-1) {
                    ItemStack stack = player.getOffhandItem();
                    inventory.offhand.add(stack);
                    player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
            }
            if ((RespawnPerkConfig.Inventory.keepInventory || RespawnPerkConfig.Hotbar.keepHotbar) && inventory.isItemsEmpty()) {
                inventory.items.clear();
                boolean onlyHotbar = RespawnPerkConfig.Hotbar.keepHotbar && !RespawnPerkConfig.Inventory.keepInventory;
                List<ItemStack> rawStacks = onlyHotbar ? player.getInventory().items.subList(0, 9) : player.getInventory().items;
                double chance = onlyHotbar ? RespawnPerkConfig.Hotbar.keepHotbarChance : RespawnPerkConfig.Inventory.keepInventoryChance;
                List<ItemStack> stacks = new ArrayList<>(rawStacks.stream().filter(i -> random.nextInt(100) <= chance).toList());
                inventory.items.addAll(stacks);
                Collections.fill(stacks, ItemStack.EMPTY);
            }
            be.storedItems.put(player.getUUID(), inventory);
            be.syncWithClient();
        }
    }
}
