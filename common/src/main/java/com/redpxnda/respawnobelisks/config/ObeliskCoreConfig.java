package com.redpxnda.respawnobelisks.config;

import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;

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
}
