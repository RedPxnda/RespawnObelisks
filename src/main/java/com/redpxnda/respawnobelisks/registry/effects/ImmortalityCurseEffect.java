package com.redpxnda.respawnobelisks.registry.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ImmortalityCurseEffect extends MobEffect {
    public ImmortalityCurseEffect() {
        super(MobEffectCategory.HARMFUL, 6225920);

        addAttributeModifier(Attributes.MAX_HEALTH, "69751bd7-0fe9-4794-b9d1-a6a71c4d9e0a", -2.0D, AttributeModifier.Operation.ADDITION);
    }

    public List<ItemStack> getCurativeItems() {
        ArrayList<ItemStack> ret = new ArrayList<>();
        ret.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        return ret;
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return false;
    }
}
