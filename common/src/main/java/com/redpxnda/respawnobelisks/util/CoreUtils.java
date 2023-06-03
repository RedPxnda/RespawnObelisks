package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.config.ObeliskCoreConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class CoreUtils {
    public static boolean hasInteraction(ObeliskCore.Instance inst, String interaction) {
        if (inst == ObeliskCore.Instance.EMPTY) return false;
        return inst.core().interactions.contains(new ResourceLocation(interaction));
    }
    public static boolean hasInteraction(ObeliskCore.Instance inst, ResourceLocation interaction) {
        if (inst == ObeliskCore.Instance.EMPTY) return false;
        return inst.core().interactions.contains(interaction);
    }
    public static boolean hasInteraction(ObeliskCore.Instance inst, ObeliskInteraction interaction) {
        if (inst == ObeliskCore.Instance.EMPTY) return false;
        return inst.core().interactions.contains(interaction.id);
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
}
