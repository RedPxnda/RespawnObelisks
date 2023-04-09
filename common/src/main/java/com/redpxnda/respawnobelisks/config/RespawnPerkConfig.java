package com.redpxnda.respawnobelisks.config;

import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.annotations.InlineCategory;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

@Category(id = "perks", translation = "text.respawnobelisks.config.perks_config")
public final class RespawnPerkConfig {
    @ConfigEntry(
            id = "keepTrinkets",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.keep_trinkets"
    )
    @Comment("Whether you should keep your curios/trinkets (if installed) when respawning at an obelisk.")
    public static boolean keepTrinkets = false;

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

    @ConfigEntry(
            id = "minKeepItemCharge",
            type = EntryType.DOUBLE,
            translation = "text.respawnobelisks.config.min_keep_item_charge"
    )
    @Comment("Minimum charge required to keep items on death when respawning at obelisks. Negative (or 0) amounts will disable this requirement.")
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
