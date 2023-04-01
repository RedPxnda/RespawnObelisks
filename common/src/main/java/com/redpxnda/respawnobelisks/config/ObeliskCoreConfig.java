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
    @Comment("Max number of revive entities that a core can hold.")
    public static int maxStoredEntities = 5;
}
