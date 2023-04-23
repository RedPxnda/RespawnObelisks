package com.redpxnda.respawnobelisks.registry.item;

import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CoreItem extends Item {
    public final List<CoreUtils.Capability> capabilities;

    public CoreItem(Properties properties, List<CoreUtils.Capability> capabilities) {
        super(properties);
        this.capabilities = capabilities;
    }

    public boolean hasCapability(CoreUtils.Capability capability) {
        return capabilities.contains(capability);
    }

    @Override
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> list) {
        if (this.allowedIn(creativeModeTab)) {
            ItemStack stack = this.getDefaultInstance();
            CoreUtils.setMaxCharge(stack.getOrCreateTag(), 100);
            CoreUtils.setCharge(stack.getOrCreateTag(), 100);
            list.add(stack);
        }
    }
}
