package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.nucleus.util.PlayerUtil;
import com.redpxnda.respawnobelisks.config.RespawnPerkConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
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
        if (
                (Object)this instanceof ServerPlayer player &&
                !player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) &&
                player.getRespawnPosition() != null &&
                player.level.getBlockEntity(player.getRespawnPosition()) instanceof RespawnObeliskBlockEntity be &&
                CoreUtils.hasInteraction(be.getCoreInstance(), ObeliskInteraction.SAVE_INV) &&
                be.getCharge(player) >= RespawnPerkConfig.minKeepItemCharge &&
                (RespawnPerkConfig.allowCursedItemKeeping || !player.hasEffect(ModRegistries.IMMORTALITY_CURSE.get()))
        ) {
            ObeliskInventory inventory = be.storedItems.containsKey(player.getUUID()) ? be.storedItems.get(player.getUUID()) : new ObeliskInventory();
            if (!player.wasExperienceConsumed() && RespawnPerkConfig.Experience.keepExperience && inventory.xp <= 0) {
                int rawXp = PlayerUtil.getTotalXp(player);
                inventory.xp = Mth.floor(rawXp*(RespawnPerkConfig.Experience.keepExperiencePercent/100f));
                if (RespawnPerkConfig.Experience.keepExperiencePercent >= 100) player.skipDropExperience();
                else player.giveExperiencePoints(-inventory.xp);
            }
            if (inventory.isArmorEmpty()) {
                inventory.armor.clear();
                List<ItemStack> stacks = new ArrayList<>(
                        player.getInventory().armor.stream().map(i -> {
                            if (
                                    (RespawnPerkConfig.Armor.keepArmor && random.nextInt(100) <= RespawnPerkConfig.Armor.keepArmorChance-1) ||
                                    (ObeliskUtils.shouldEnchantmentApply(i, random))
                            ) {
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
            if (!player.getOffhandItem().isEmpty() && inventory.isOffhandEmpty()) {
                inventory.offhand.clear();
                if (
                        (RespawnPerkConfig.Offhand.keepOffhand && random.nextInt(100) <= RespawnPerkConfig.Offhand.keepOffhandChance-1) ||
                        (ObeliskUtils.shouldEnchantmentApply(player.getOffhandItem(), random))
                ) {
                    inventory.offhand.add(player.getOffhandItem());
                    player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
            }
            if (inventory.isItemsEmpty()) {
                inventory.items.clear();
                boolean onlyHotbar = RespawnPerkConfig.Hotbar.keepHotbar && !RespawnPerkConfig.Inventory.keepInventory;
                List<ItemStack> rawStacks = onlyHotbar ? player.getInventory().items.subList(0, 9) : player.getInventory().items;
                double chance = onlyHotbar ? RespawnPerkConfig.Hotbar.keepHotbarChance : RespawnPerkConfig.Inventory.keepInventoryChance;
                List<ItemStack> stacks = new ArrayList<>(rawStacks.stream().map(i -> {
                    if (
                            ((RespawnPerkConfig.Inventory.keepInventory || RespawnPerkConfig.Hotbar.keepHotbar) && random.nextInt(100) <= chance) ||
                            (ObeliskUtils.shouldEnchantmentApply(i, random))
                    ) {
                        int index = player.getInventory().items.indexOf(i);
                        player.getInventory().items.set(index, ItemStack.EMPTY);
                        return i;
                    } else {
                        return ItemStack.EMPTY;
                    }
                }).toList());
                inventory.items.addAll(stacks);
            }
            be.storedItems.put(player.getUUID(), inventory);
            be.syncWithClient();
        }
    }
}
