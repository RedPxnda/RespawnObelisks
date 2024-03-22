package com.redpxnda.respawnobelisks.forge.compat;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.Random;

public class CuriosCompat {
    private static final Random random = new Random();

    public static void onDropRules(DropRulesEvent event) {
        event.addOverride(stack ->
                ObeliskUtils.shouldEnchantmentApply(stack, random) ||
                (RespawnObelisksConfig.INSTANCE.respawnPerks.trinkets.keepTrinkets && random.nextInt(100) <= RespawnObelisksConfig.INSTANCE.respawnPerks.trinkets.keepTrinketsChance - 1),
                ICurio.DropRule.ALWAYS_KEEP
        );
    }
}
