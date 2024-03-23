package com.redpxnda.respawnobelisks.fabric.compat;

import com.redpxnda.respawnobelisks.facet.kept.KeptItemsModule;

public class TrinketsCompat {
    public static void init() {
        /*TrinketDropCallback.EVENT.register((rule, stack, ref, entity) -> {
            if (ObeliskUtils.shouldSaveItem(RespawnObelisksConfig.INSTANCE.respawnPerks.trinkets.keepTrinkets, RespawnObelisksConfig.INSTANCE.respawnPerks.trinkets.keepTrinketsChance, stack))
                return TrinketEnums.DropRule.KEEP;
            return rule;
        });*/
        KeptItemsModule.registerModule("trinkets", player -> new KeptTrinketsModule());
    }
}
