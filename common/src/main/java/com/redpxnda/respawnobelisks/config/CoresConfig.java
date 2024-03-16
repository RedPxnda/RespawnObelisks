package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Comment;
import com.redpxnda.respawnobelisks.data.listener.ObeliskCore;
import net.minecraft.util.Identifier;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

@ConfigAutoCodec.ConfigClassMarker
public class CoresConfig {
    @Comment("Max number of revive entities that a core can hold.\nI recommend keeping this at a low amount, as large amounts (>10? Haven't tested values greater.) can cause issues with data storage.")
    public int maxStoredEntities = 5;

    @Comment("The maximum amount of 'max radiance' an obelisk can have. (Cores can be upgraded in order to reach this amount, but cannot go over.)")
    public int maxMaxRadiance = 1000;

    @Comment("The default core inside wild obelisks. (Addons can easily create new cores, this is used for that.)")
    public Identifier defaultCore = new Identifier(MOD_ID, "obelisk_core");
    public ObeliskCore getDefaultCore() {
        return ObeliskCore.CORES.getOrDefault(defaultCore, ObeliskCore.ANCIENT_CORE);
    }

    @Comment("The chance for wild obelisks to have a core. (in %)")
    public double wildCoreChance = 100;

    @Comment("The minimum amount of radiance a wild obelisk will spawn with. (inclusive)")
    public int wildMinRadiance = 100;

    @Comment("The maximum amount of radiance a wild obelisk will spawn with. (exclusive)")
    public int wildMaxRadiance = 101;

    @Comment("The minimum amount of max radiance a wild obelisk will spawn with. (inclusive)")
    public int wildMinMaxRadiance = 100;

    @Comment("The maximum amount of max radiance a wild obelisk will spawn with. (exclusive)")
    public int wildMaxMaxRadiancee = 101;
}
