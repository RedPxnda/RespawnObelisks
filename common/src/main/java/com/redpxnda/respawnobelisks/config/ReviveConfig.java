package com.redpxnda.respawnobelisks.config;

import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;

@Category(id = "revive", translation = "text.respawnobelisks.config.revive_config")
public final class ReviveConfig {
    @ConfigEntry(
            id = "enableRevival",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.enable_revival"
    )
    @Comment("Whether entity revival via respawn obelisk should be enabled.")
    public static boolean enableRevival = true;

    @ConfigEntry(
            id = "revivalItem",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.revival_item"
    )
    @Comment("Item used to revive an obelisk's saved entities.")
    public static String revivalItem = "minecraft:totem_of_undying";

    @ConfigEntry(
            id = "maxRevivalEntities",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.max_revive_entities"
    )
    @Comment("Max number of entities that can be revived at once.")
    public static int maxEntities = 3;

    @ConfigEntry(
            id = "revivableEntities",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.revivable_entities"
    )
    @Comment("""
        Whitelist for all entities that can be revived. (Tags are supported)
        Any entry beginning with '$' is a hardcoded option that allows for the respective type to pass through.
        Available '$'s:
        '$tamables' (any tamable entity),
        '$animals' (pigs, cows, sheep, etc.),
        '$merchants' (villagers)"""
    )
    public static String[] revivableEntities = {"$tamables", "$animals", "$merchants"};

    @ConfigEntry(
            id = "revivableEntitiesBlacklist",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.revivable_entities_blacklist"
    )
    @Comment("Whether the revivable entities list should act as a blacklist.")
    public static boolean entitiesIsBlacklist = false;

    @ConfigEntry(
            id = "revivalCost",
            type = EntryType.DOUBLE,
            translation = "text.respawnobelisks.config.revival_cost"
    )
    @Comment("Obelisk depletion amount when reviving entities.")
    public static double revivalCost = 10;
}
