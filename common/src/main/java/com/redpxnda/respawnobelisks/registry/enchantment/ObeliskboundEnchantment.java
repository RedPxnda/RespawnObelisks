package com.redpxnda.respawnobelisks.registry.enchantment;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ObeliskboundEnchantment extends Enchantment {
    public ObeliskboundEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.BREAKABLE, EquipmentSlot.values());
    }

    @Override
    public boolean isAcceptableItem(ItemStack itemStack) {
        return itemStack.getItem().getMaxCount() <= 1 || super.isAcceptableItem(itemStack);
    }

    @Override
    public int getMaxLevel() {
        return RespawnObelisksConfig.INSTANCE.respawnPerks.enchantmentConfig.maxLevel;
    }
    @Override
    public boolean isTreasure() {
        return RespawnObelisksConfig.INSTANCE.respawnPerks.enchantmentConfig.treasureOnly;
    }
    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return RespawnObelisksConfig.INSTANCE.respawnPerks.enchantmentConfig.tradeable;
    }
    @Override
    public boolean isAvailableForRandomSelection() {
        return RespawnObelisksConfig.INSTANCE.respawnPerks.enchantmentConfig.discoverable;
    }
}
