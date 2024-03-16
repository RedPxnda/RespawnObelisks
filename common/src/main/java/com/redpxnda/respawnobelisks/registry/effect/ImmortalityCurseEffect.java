package com.redpxnda.respawnobelisks.registry.effect;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ImmortalityCurseEffect extends StatusEffect {
    public ImmortalityCurseEffect() {
        super(StatusEffectCategory.HARMFUL, 6225920);

        addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, "69751bd7-0fe9-4794-b9d1-a6a71c4d9e0a", -2.0D, EntityAttributeModifier.Operation.ADDITION);
    }

    public List<ItemStack> getCurativeItems() {
        ArrayList<ItemStack> ret = new ArrayList<>();
        ret.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        return ret;
    }

    public boolean canApplyUpdateEffect(int pDuration, int pAmplifier) {
        return false;
    }
}