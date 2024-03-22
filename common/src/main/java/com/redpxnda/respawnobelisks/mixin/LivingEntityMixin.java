package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.nucleus.math.MathUtil;
import com.redpxnda.nucleus.util.PlayerUtil;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.ObeliskInventory;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantValue")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(
            method = "drop",
            at = @At("HEAD"),
            cancellable = true)
    private void RESPAWNOBELISKS_preventEquipmentDrop(DamageSource damageSource, CallbackInfo ci) {
        if (((LivingEntity) ((Object) this)).getCommandTags().contains("respawnobelisks:no_drops_entity"))
            ci.cancel();
        if (
                (Object) this instanceof ServerPlayerEntity player &&
                !player.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY) &&
                player.getSpawnPointPosition() != null &&
                player.getWorld().getBlockEntity(player.getSpawnPointPosition()) instanceof RespawnObeliskBlockEntity be &&
                CoreUtils.hasInteraction(be.getCoreInstance(), ObeliskInteraction.SAVE_INV) &&
                be.getCharge(player) >= RespawnObelisksConfig.INSTANCE.respawnPerks.minKeepItemRadiance &&
                (RespawnObelisksConfig.INSTANCE.respawnPerks.allowCursedItemKeeping || !player.hasStatusEffect(ModRegistries.immortalityCurse.get()))
        ) {
            ObeliskInventory inventory = be.storedItems.containsKey(player.getUuid()) ? be.storedItems.get(player.getUuid()) : new ObeliskInventory();
            if (!player.isExperienceDroppingDisabled() && RespawnObelisksConfig.INSTANCE.respawnPerks.experience.keepExperience && inventory.xp <= 0) {
                int rawXp = PlayerUtil.getTotalXp(player);
                inventory.xp = MathHelper.floor(rawXp*(RespawnObelisksConfig.INSTANCE.respawnPerks.experience.keepExperiencePercent/100f));
                if (RespawnObelisksConfig.INSTANCE.respawnPerks.experience.keepExperiencePercent >= 100) player.disableExperienceDropping();
                else player.addExperience(-inventory.xp);
            }
            if (inventory.isArmorEmpty()) {
                inventory.armor.clear();
                List<ItemStack> stacks = new ArrayList<>(
                        player.getInventory().armor.stream().map(i -> {
                            if (
                                    (RespawnObelisksConfig.INSTANCE.respawnPerks.armor.keepArmor && MathUtil.random.nextInt(100) <= RespawnObelisksConfig.INSTANCE.respawnPerks.armor.keepArmorChance-1) ||
                                    (ObeliskUtils.shouldEnchantmentApply(i, MathUtil.random))
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
            if (!player.getOffHandStack().isEmpty() && inventory.isOffhandEmpty()) {
                inventory.offhand.clear();
                if (
                        (RespawnObelisksConfig.INSTANCE.respawnPerks.offhand.keepOffhand && MathUtil.random.nextInt(100) <= RespawnObelisksConfig.INSTANCE.respawnPerks.offhand.keepOffhandChance-1) ||
                        (ObeliskUtils.shouldEnchantmentApply(player.getOffHandStack(), MathUtil.random))
                ) {
                    inventory.offhand.add(player.getOffHandStack());
                    player.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
            }
            if (inventory.isItemsEmpty()) {
                inventory.items.clear();
                boolean onlyHotbar = RespawnObelisksConfig.INSTANCE.respawnPerks.hotbar.keepHotbar && !RespawnObelisksConfig.INSTANCE.respawnPerks.inventory.keepInventory;
                List<ItemStack> rawStacks = onlyHotbar ? player.getInventory().main.subList(0, 9) : player.getInventory().main;
                double chance = onlyHotbar ? RespawnObelisksConfig.INSTANCE.respawnPerks.hotbar.keepHotbarChance : RespawnObelisksConfig.INSTANCE.respawnPerks.inventory.keepInventoryChance;
                List<ItemStack> stacks = new ArrayList<>(rawStacks.stream().map(i -> {
                    if (
                            ((RespawnObelisksConfig.INSTANCE.respawnPerks.inventory.keepInventory || RespawnObelisksConfig.INSTANCE.respawnPerks.hotbar.keepHotbar) && MathUtil.random.nextInt(100) <= chance) ||
                            (ObeliskUtils.shouldEnchantmentApply(i, MathUtil.random))
                    ) {
                        int index = player.getInventory().main.indexOf(i);
                        player.getInventory().main.set(index, ItemStack.EMPTY);
                        return i;
                    } else {
                        return ItemStack.EMPTY;
                    }
                }).toList());
                inventory.items.addAll(stacks);
            }
            be.storedItems.put(player.getUuid(), inventory);
            be.syncWithClient();
        }
    }
}
