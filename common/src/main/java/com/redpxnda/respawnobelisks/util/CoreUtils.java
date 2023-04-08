package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.registry.item.CoreItem;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CoreUtils {
    public static List<Capability> DEFAULT_CAPS = List.of(Capability.CHARGE, Capability.TELEPORT, Capability.REVIVE, Capability.PROTECT, Capability.SAVE_INV);

    public static boolean hasCapability(ItemStack stack, Capability capability) {
        if (stack.hasTag() && stack.getTag().contains("RespawnObeliskData", 10) && stack.getTag().getCompound("RespawnObeliskData").contains("CoreCaps", 9)) {
            ListTag tag = stack.getTag().getCompound("RespawnObeliskData").getList("CoreCaps", 8);
            return tag.contains(StringTag.valueOf(capability.capTag));
        }
        if (stack.getItem() instanceof CoreItem item)
            return item.hasCapability(capability);
        return false;
    }

    public record Capability(String capTag) {
            public static final Capability CHARGE = new Capability("charge");
            public static final Capability TELEPORT = new Capability("teleport");
            public static final Capability REVIVE = new Capability("revive");
            public static final Capability PROTECT = new Capability("trust");
            public static final Capability SAVE_INV = new Capability("soulbound");
    }
}
