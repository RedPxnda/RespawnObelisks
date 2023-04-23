package com.redpxnda.respawnobelisks.registry.enchantment;

import com.redpxnda.respawnobelisks.config.RespawnPerkConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class ObeliskboundEnchantment extends Enchantment {
    public ObeliskboundEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
    }

    @Override
    public boolean canEnchant(ItemStack itemStack) {
        return itemStack.getItem().getMaxStackSize() <= 1 || super.canEnchant(itemStack);
    }

    @Override
    public int getMaxLevel() {
        return RespawnPerkConfig.Enchantment.maxLevel;
    }
    @Override
    public boolean isTreasureOnly() {
        return RespawnPerkConfig.Enchantment.treasureOnly;
    }
    @Override
    public boolean isTradeable() {
        return RespawnPerkConfig.Enchantment.tradeable;
    }
    @Override
    public boolean isDiscoverable() {
        return RespawnPerkConfig.Enchantment.discoverable;
    }
}
