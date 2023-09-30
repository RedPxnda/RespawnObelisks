package com.redpxnda.respawnobelisks.config;

import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.annotations.InlineCategory;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;

@Category(id = "perks", translation = "text.respawnobelisks.config.perks_config")
public final class RespawnPerkConfig {
    @ConfigEntry(
            id = "minKeepItemCharge",
            type = EntryType.DOUBLE,
            translation = "text.respawnobelisks.config.min_keep_item_charge"
    )
    @Comment("Minimum charge required to keep items on death when respawning at obelisks. Negative amounts (or 0) will disable this requirement.")
    public static double minKeepItemCharge = 1;

    @ConfigEntry(
            id = "allowCursedItemKeeping",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.allow_cursed_item_keep"
    )
    @Comment("Whether players can keep items on death even when they have the immortality curse.")
    public static boolean allowCursedItemKeeping = false;

    @InlineCategory
    public static Inventory inventoryConfig;

    @InlineCategory
    public static Hotbar hotbarConfig;

    @InlineCategory
    public static Armor armorConfig;

    @InlineCategory
    public static Offhand offhandConfig;

    @InlineCategory
    public static Experience experienceConfig;

    @InlineCategory
    public static Trinkets trinketsConfig;


    @Category(id = "obeliskbound", translation = "text.respawnobelisks.config.obeliskbound_config")
    public final static class Enchantment {
        @ConfigEntry(
                id = "enableEnchantment",
                type = EntryType.BOOLEAN,
                translation = "text.respawnobelisks.config.enable_enchantment"
        )
        @Comment("Whether the obeliskbound enchantment should be enabled.")
        public static boolean enableEnchantment = true;

        @ConfigEntry(
                id = "treasureOnly",
                type = EntryType.BOOLEAN,
                translation = "text.respawnobelisks.config.treasure_only"
        )
        @Comment("Whether the obeliskbound enchantment should be treasure only. (Can't find in enchantment table)")
        public static boolean treasureOnly = true;

        @ConfigEntry(
                id = "tradeable",
                type = EntryType.BOOLEAN,
                translation = "text.respawnobelisks.config.tradeable"
        )
        @Comment("Whether the obeliskbound enchantment can be found in villager trades.")
        public static boolean tradeable = false;

        @ConfigEntry(
                id = "discoverable",
                type = EntryType.BOOLEAN,
                translation = "text.respawnobelisks.config.discoverable"
        )
        @Comment("Whether the obeliskbound enchantment can be discoverable. (Found throughout chests in world)")
        public static boolean discoverable = true;

        @ConfigEntry(
                id = "chancePerLevel",
                type = EntryType.DOUBLE,
                translation = "text.respawnobelisks.config.chance_per_level"
        )
        @Comment("The chance (%) per level to keep items with the obeliskbound enchantment.")
        public static double chancePerLevel = 25;

        @ConfigEntry(
                id = "maxLevel",
                type = EntryType.INTEGER,
                translation = "text.respawnobelisks.config.max_level"
        )
        @Comment("The highest level the obeliskbound enchantment can have.")
        public static int maxLevel = 3;
    }

    @Category(id = "keepExperience", translation = "text.respawnobelisks.config.keep_experience_config")
    public final static class Experience {
        @ConfigEntry(
                id = "keepExperience",
                type = EntryType.BOOLEAN,
                translation = "text.respawnobelisks.config.keep_experience"
        )
        @Comment("Whether you should keep experience after death when respawning at an obelisk.")
        public static boolean keepExperience = false;

        @ConfigEntry(
                id = "keepExperiencePercent",
                type = EntryType.DOUBLE,
                translation = "text.respawnobelisks.config.keep_experience_percent"
        )
        @Comment("How much (%) experience should be kept when respawning at an obelisk.")
        public static double keepExperiencePercent = 100;
    }

    @Category(id = "keepTrinkets", translation = "text.respawnobelisks.config.keep_trinkets_config")
    public final static class Trinkets {
        @ConfigEntry(
                id = "keepTrinkets",
                type = EntryType.BOOLEAN,
                translation = "text.respawnobelisks.config.keep_trinkets"
        )
        @Comment("Whether you should keep your curios/trinkets (if installed) when respawning at an obelisk.")
        public static boolean keepTrinkets = false;

        @ConfigEntry(
                id = "keepTrinketsChance",
                type = EntryType.DOUBLE,
                translation = "text.respawnobelisks.config.keep_trinket_chance"
        )
        @Comment("The chance (%) to keep each trinket in your trinket inventory.")
        public static double keepTrinketsChance = 100;
    }

    @Category(id = "keepInv", translation = "text.respawnobelisks.config.keep_inv_config")
    public final static class Inventory {
        @ConfigEntry(
                id = "keepInventory",
                type = EntryType.BOOLEAN,
                translation = "text.respawnobelisks.config.keep_inventory"
        )
        @Comment("Whether you should keep your inventory (discluding armor and offhand) when respawning at an obelisk.")
        public static boolean keepInventory = false;

        @ConfigEntry(
                id = "keepInventoryChance",
                type = EntryType.DOUBLE,
                translation = "text.respawnobelisks.config.keep_inventory_chance"
        )
        @Comment("The chance (%) to keep each item in your inventory.")
        public static double keepInventoryChance = 100;
    }

    @Category(id = "keepHotbar", translation = "text.respawnobelisks.config.keep_hotbar_config")
    public final static class Hotbar {
        @ConfigEntry(
                id = "keepHotbar",
                type = EntryType.BOOLEAN,
                translation = "text.respawnobelisks.config.keep_hotbar"
        )
        @Comment("Whether you should keep your hotbar when respawning at an obelisk.")
        public static boolean keepHotbar = false;

        @ConfigEntry(
                id = "keepHotbarChance",
                type = EntryType.DOUBLE,
                translation = "text.respawnobelisks.config.keep_hotbar_chance"
        )
        @Comment("The chance (%) to keep each item in your hotbar.")
        public static double keepHotbarChance = 100;
    }

    @Category(id = "keepArmor", translation = "text.respawnobelisks.config.keep_armor_config")
    public final static class Armor {
        @ConfigEntry(
                id = "keepArmor",
                type = EntryType.BOOLEAN,
                translation = "text.respawnobelisks.config.keep_armor"
        )
        @Comment("Whether you should keep your armor when respawning at an obelisk.")
        public static boolean keepArmor = false;

        @ConfigEntry(
                id = "keepArmorChance",
                type = EntryType.DOUBLE,
                translation = "text.respawnobelisks.config.keep_armor_chance"
        )
        @Comment("The chance (%) to keep each armor piece.")
        public static double keepArmorChance = 100;
    }

    @Category(id = "keepOffhand", translation = "text.respawnobelisks.config.keep_offhand_config")
    public final static class Offhand {
        @ConfigEntry(
                id = "keepOffhand",
                type = EntryType.BOOLEAN,
                translation = "text.respawnobelisks.config.keep_offhand"
        )
        @Comment("Whether you should keep your offhand item when respawning at an obelisk.")
        public static boolean keepOffhand = false;
        @ConfigEntry(
                id = "keepOffhandChance",
                type = EntryType.DOUBLE,
                translation = "text.respawnobelisks.config.keep_offhand_chance"
        )
        @Comment("The chance (%) to keep the item in your offhand.")
        public static double keepOffhandChance = 100;
    }
}
