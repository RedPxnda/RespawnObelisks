package com.redpxnda.respawnobelisks.fabric.compat;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import dev.emi.trinkets.api.TrinketEnums;
import dev.emi.trinkets.api.event.TrinketDropCallback;

import java.util.Random;

public class TrinketsCompat {
    private static final Random random = new Random();

    public static void init() {
        TrinketDropCallback.EVENT.register((rule, stack, ref, entity) -> {
            if (
                    ObeliskUtils.shouldEnchantmentApply(stack, random) ||
                    (RespawnObelisksConfig.INSTANCE.respawnPerks.trinkets.keepTrinkets && random.nextInt(100) <= RespawnObelisksConfig.INSTANCE.respawnPerks.trinkets.keepTrinketsChance-1)
            )
                return TrinketEnums.DropRule.KEEP;
            return rule;
        });
    }
}
