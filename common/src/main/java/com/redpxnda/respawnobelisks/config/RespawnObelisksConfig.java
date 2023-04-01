package com.redpxnda.respawnobelisks.config;

import com.teamresourceful.resourcefulconfig.common.annotations.Config;
import com.teamresourceful.resourcefulconfig.common.annotations.InlineCategory;
import com.teamresourceful.resourcefulconfig.web.annotations.WebInfo;

@Config("respawnobelisks")
@WebInfo(icon = "smartphone-charging")
public final class RespawnObelisksConfig {
    @InlineCategory
    public static CurseConfig curseConfig;

    @InlineCategory
    public static ChargeConfig chargeConfig;

    @InlineCategory
    public static ReviveConfig reviveConfig;

    @InlineCategory
    public static TrustedPlayersConfig trustedPlayersConfig;

    @InlineCategory
    public static ObeliskCoreConfig obeliskCoreConfig;
}
