package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class CoreUtils {
    public static boolean hasInteraction(ObeliskCore.Instance inst, String interaction) {
        if (inst == ObeliskCore.Instance.EMPTY) return false;
        return inst.core().interactions.contains(new Identifier(interaction));
    }
    public static boolean hasInteraction(ObeliskCore.Instance inst, Identifier interaction) {
        if (inst == ObeliskCore.Instance.EMPTY) return false;
        return inst.core().interactions.contains(interaction);
    }
    public static boolean hasInteraction(ObeliskCore.Instance inst, ObeliskInteraction interaction) {
        if (inst == ObeliskCore.Instance.EMPTY) return false;
        return inst.core().interactions.contains(interaction.id);
    }

    public static double getCharge(NbtCompound tag) {
        return tag.getCompound("RespawnObeliskData").getDouble("Charge");
    }
    public static void setCharge(NbtCompound tag, double charge) {
        if (!tag.contains("RespawnObeliskData", 10)) tag.put("RespawnObeliskData", new NbtCompound());
        tag.getCompound("RespawnObeliskData").putDouble("Charge", charge);
    }
    public static double getMaxCharge(NbtCompound tag) {
        return Math.min(RespawnObelisksConfig.INSTANCE.cores.maxMaxRadiance, tag.getCompound("RespawnObeliskData").getDouble("MaxCharge"));
    }
    public static String getTextMaxCharge(NbtCompound tag) {
        return String.valueOf(tag.getCompound("RespawnObeliskData").get("MaxCharge"));
    }
    public static void setMaxCharge(NbtCompound tag, double charge) {
        if (!tag.contains("RespawnObeliskData", 10)) tag.put("RespawnObeliskData", new NbtCompound());
        tag.getCompound("RespawnObeliskData").putDouble("MaxCharge", Math.min(RespawnObelisksConfig.INSTANCE.cores.maxMaxRadiance, charge));
    }

    public static void setMaxCharge(NbtCompound tag, String charge) {
        if (!tag.contains("RespawnObeliskData", 10)) tag.put("RespawnObeliskData", new NbtCompound());
        tag.getCompound("RespawnObeliskData").putString("MaxCharge", charge);
    }

    public static void incMaxCharge(NbtCompound tag, double charge) {
        setMaxCharge(tag, getMaxCharge(tag)+charge);
    }
}
