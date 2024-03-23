package com.redpxnda.respawnobelisks.forge.compat;

import com.redpxnda.respawnobelisks.facet.kept.KeptItemsModule;

public class CuriosCompat {
    public static void init() {
        KeptItemsModule.registerModule("curios", player -> new KeptCuriosModule());
    }

    /*public static void onDropRules(DropRulesEvent event) {
        event.addOverride(stack ->
                ObeliskUtils.shouldEnchantmentApply(stack, random) ||
                (RespawnObelisksConfig.INSTANCE.respawnPerks.trinkets.keepTrinkets && random.nextInt(100) <= RespawnObelisksConfig.INSTANCE.respawnPerks.trinkets.keepTrinketsChance - 1),
                ICurio.DropRule.ALWAYS_KEEP
        );
    }*/
}
