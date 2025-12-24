package com.redpxnda.respawnobelisks.registry.enchantment;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
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
        return RespawnObelisksConfig.INSTANCE.respawnPerks.enchantment.maxLevel;
    }
    @Override
    public boolean isTreasureOnly() {
        return RespawnObelisksConfig.INSTANCE.respawnPerks.enchantment.treasureOnly;
    }
    @Override
    public boolean isTradeable() {
        return RespawnObelisksConfig.INSTANCE.respawnPerks.enchantment.tradeable;
    }
    @Override
    public boolean isDiscoverable() {
        return RespawnObelisksConfig.INSTANCE.respawnPerks.enchantment.discoverable;
    }
}
