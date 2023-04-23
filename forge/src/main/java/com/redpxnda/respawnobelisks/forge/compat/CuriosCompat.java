package com.redpxnda.respawnobelisks.forge.compat;

import com.redpxnda.respawnobelisks.config.RespawnPerkConfig;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.Random;

public class CuriosCompat {
    private static Random random = new Random();

    public static void onDropRules(DropRulesEvent event) {
        event.addOverride(stack ->
                ObeliskUtils.shouldEnchantmentApply(stack, random) ||
                (RespawnPerkConfig.Trinkets.keepTrinkets && random.nextInt(100) <= RespawnPerkConfig.Trinkets.keepTrinketsChance - 1),
                ICurio.DropRule.ALWAYS_KEEP
        );
    }
}
