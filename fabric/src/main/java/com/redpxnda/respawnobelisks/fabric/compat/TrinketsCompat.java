package com.redpxnda.respawnobelisks.fabric.compat;

import com.redpxnda.respawnobelisks.config.RespawnPerkConfig;
import com.redpxnda.respawnobelisks.util.ObeliskUtils;
import dev.emi.trinkets.api.TrinketEnums;
import dev.emi.trinkets.api.event.TrinketDropCallback;

import java.util.Random;

public class TrinketsCompat {
    private static Random random = new Random();

    public static void init() {
        TrinketDropCallback.EVENT.register((rule, stack, ref, entity) -> {
            if (
                    ObeliskUtils.shouldEnchantmentApply(stack, random) ||
                    (RespawnPerkConfig.Trinkets.keepTrinkets && random.nextInt(100) <= RespawnPerkConfig.Trinkets.keepTrinketsChance-1)
            )
                return TrinketEnums.DropRule.KEEP;
            return rule;
        });
    }
}
