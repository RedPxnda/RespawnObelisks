package com.redpxnda.respawnobelisks.registry.item;

import com.redpxnda.respawnobelisks.util.CoreUtils;
import net.minecraft.world.item.Item;

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
}
