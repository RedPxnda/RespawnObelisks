package com.redpxnda.respawnobelisks.config;

import com.redpxnda.respawnobelisks.data.listener.ObeliskCore;
import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;
import net.minecraft.resources.ResourceLocation;

@Category(id = "cores", translation = "text.respawnobelisks.config.cores_config")
public final class ObeliskCoreConfig {
    @ConfigEntry(
            id = "maxStoredEntities",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.max_stored_entities"
    )
    @Comment("Max number of revive entities that a core can hold.\nI recommend keeping this at a low amount, as large amounts (>10? Haven't tested values greater.) can cause issues with data storage.")
    public static int maxStoredEntities = 5;

    @ConfigEntry(
            id = "maxMaxCharge",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.max_max_charge"
    )
    @Comment("The maximum amount of 'max charge' an obelisk can have. (Cores can be upgraded in order to reach this amount, but cannot go over.)")
    public static int maxMaxCharge = 1000;

    @ConfigEntry(
            id = "defaultCore",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.default_core"
    )
    @Comment("The default core inside wild obelisks. (Addons can easily create new cores, this is used for that.)")
    public static String defaultCore = "respawnobelisks:obelisk_core";
    public static ObeliskCore getDefaultCore() {
        return ObeliskCore.CORES.getOrDefault(new ResourceLocation(defaultCore), ObeliskCore.ANCIENT_CORE);
    }

    @ConfigEntry(
            id = "wildCoreChance",
            type = EntryType.DOUBLE,
            translation = "text.respawnobelisks.config.wild_core_chance"
    )
    @Comment("The chance for wild obelisks to have a core. (in %)")
    public static double wildCoreChance = 100;

    @ConfigEntry(
            id = "wildMinCharge",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.wild_min_charge"
    )
    @Comment("The minimum amount of charge (%) a wild obelisk will spawn with. (inclusive)")
    public static int wildMinCharge = 100;

    @ConfigEntry(
            id = "wildMaxCharge",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.wild_max_charge"
    )
    @Comment("The maximum amount of charge (%) a wild obelisk will spawn with. (exclusive, in %)")
    public static int wildMaxCharge = 101;

    @ConfigEntry(
            id = "wildMinMaxCharge",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.wild_min_max_charge"
    )
    @Comment("The minimum amount of max charge a wild obelisk will spawn with. (inclusive)")
    public static int wildMinMaxCharge = 100;

    @ConfigEntry(
            id = "wildMaxMaxCharge",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.wild_max_max_charge"
    )
    @Comment("The maximum amount of max charge a wild obelisk will spawn with. (exclusive)")
    public static int wildMaxMaxCharge = 101;
}
