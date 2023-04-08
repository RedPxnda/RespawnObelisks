package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.respawnobelisks.config.RespawnPerkConfig;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import com.redpxnda.respawnobelisks.util.ObeliskInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
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
                CoreUtils.hasCapability(be.getItemStack(), CoreUtils.Capability.SAVE_INV) &&
                be.getCharge(player) > 0
        ) {
            ObeliskInventory inventory = be.storedItems.containsKey(player.getUUID()) ? be.storedItems.get(player.getUUID()) : new ObeliskInventory();
            if (RespawnPerkConfig.keepArmor && inventory.isEmpty()) {
                inventory.armor.addAll(player.getInventory().armor);
                Collections.fill(player.getInventory().armor, ItemStack.EMPTY);
            }
            if (RespawnPerkConfig.keepOffhand && !player.getOffhandItem().isEmpty() && inventory.offhand.isEmpty()) {
                inventory.offhand.addAll(player.getInventory().offhand);
                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
            if ((RespawnPerkConfig.keepInventory || RespawnPerkConfig.keepHotbar) && inventory.items.isEmpty()) {
                boolean onlyHotbar = RespawnPerkConfig.keepHotbar && !RespawnPerkConfig.keepInventory;
                List<ItemStack> items = onlyHotbar ? player.getInventory().items.subList(0, 9) : player.getInventory().items;
                inventory.items.addAll(items);
                Collections.fill(items, ItemStack.EMPTY);
            }
            be.storedItems.put(player.getUUID(), inventory);
            be.syncWithClient();
        }
    }
}
