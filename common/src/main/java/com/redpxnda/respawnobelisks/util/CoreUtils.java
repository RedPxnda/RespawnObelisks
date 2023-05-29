package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.config.ObeliskCoreConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

public class CoreUtils {
    public static List<Capability> DEFAULT_CAPS = List.of(Capability.CHARGE, Capability.TELEPORT, Capability.REVIVE, Capability.PROTECT, Capability.SAVE_INV);

    public static boolean hasCapability(ObeliskCore.Instance instance, Capability capability) {
        if (instance == ObeliskCore.Instance.EMPTY) return false;
        return instance.core().capabilities.contains(capability);
    }

    public static double getCharge(CompoundTag tag) {
        return tag.getCompound("RespawnObeliskData").getDouble("Charge");
    }
    public static void setCharge(CompoundTag tag, double charge) {
        if (!tag.contains("RespawnObeliskData", 10)) tag.put("RespawnObeliskData", new CompoundTag());
        tag.getCompound("RespawnObeliskData").putDouble("Charge", charge);
    }
    public static double getMaxCharge(CompoundTag tag) {
        return Math.min(ObeliskCoreConfig.maxMaxCharge, tag.getCompound("RespawnObeliskData").getDouble("MaxCharge"));
    }
    public static String getTextMaxCharge(CompoundTag tag) {
        return String.valueOf(tag.getCompound("RespawnObeliskData").get("MaxCharge"));
    }
    public static void setMaxCharge(CompoundTag tag, double charge) {
        if (!tag.contains("RespawnObeliskData", 10)) tag.put("RespawnObeliskData", new CompoundTag());
        tag.getCompound("RespawnObeliskData").putDouble("MaxCharge", Math.min(ObeliskCoreConfig.maxMaxCharge, charge));
    }

    public static void setMaxCharge(CompoundTag tag, String charge) {
        if (!tag.contains("RespawnObeliskData", 10)) tag.put("RespawnObeliskData", new CompoundTag());
        tag.getCompound("RespawnObeliskData").putString("MaxCharge", charge);
    }

    public static void incMaxCharge(CompoundTag tag, double charge) {
        setMaxCharge(tag, getMaxCharge(tag)+charge);
    }

    public record Capability(String capTag) {
            public static final Capability CHARGE = new Capability("charge");
            public static final Capability TELEPORT = new Capability("teleport");
            public static final Capability REVIVE = new Capability("revive");
            public static final Capability PROTECT = new Capability("trust");
            public static final Capability SAVE_INV = new Capability("soulbound");
    }
}
