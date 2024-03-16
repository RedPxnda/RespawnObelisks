package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Comment;

@ConfigAutoCodec.ConfigClassMarker
public class RespawnPerksConfig {
    @Comment("Minimum radiance required to keep items on death when respawning at obelisks. Negative amounts (or 0) will disable this requirement.")
    public double minKeepItemRadiance = 1;

    @Comment("Whether players can keep items on death even when they have the immortality curse.")
    public boolean allowCursedItemKeeping = false;

    public Enchantment enchantmentConfig = new Enchantment();

    public Inventory inventoryConfig = new Inventory();

    public Hotbar hotbarConfig = new Hotbar();

    public Armor armorConfig = new Armor();

    public Offhand offhandConfig = new Offhand();

    public Experience experienceConfig = new Experience();

    public Trinkets trinketsConfig = new Trinkets();


    @ConfigAutoCodec.ConfigClassMarker
    public static class Enchantment {
        @Comment("Whether the obeliskbound enchantment should be enabled.")
        public boolean enableEnchantment = true;

        @Comment("Whether the obeliskbound enchantment should be treasure only. (Can't find in enchantment table)")
        public boolean treasureOnly = true;

        @Comment("Whether the obeliskbound enchantment can be found in villager trades.")
        public boolean tradeable = false;

        @Comment("Whether the obeliskbound enchantment can be discoverable. (Found throughout chests in world)")
        public boolean discoverable = true;

        @Comment("The chance (%) per level to keep items with the obeliskbound enchantment.")
        public double chancePerLevel = 25;

        @Comment("The highest level the obeliskbound enchantment can have.")
        public int maxLevel = 3;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class Experience {
        @Comment("Whether you should keep experience after death when respawning at an obelisk.")
        public boolean keepExperience = false;

        @Comment("How much (%) experience should be kept when respawning at an obelisk.")
        public double keepExperiencePercent = 100;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class Trinkets {
        @Comment("Whether you should keep your curios/trinkets (if installed) when respawning at an obelisk.")
        public boolean keepTrinkets = false;

        @Comment("The chance (%) to keep each trinket in your trinket inventory.")
        public double keepTrinketsChance = 100;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class Inventory {
        @Comment("Whether you should keep your inventory (discluding armor and offhand) when respawning at an obelisk.")
        public boolean keepInventory = false;

        @Comment("The chance (%) to keep each item in your inventory.")
        public double keepInventoryChance = 100;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class Hotbar {
        @Comment("Whether you should keep your hotbar when respawning at an obelisk.")
        public boolean keepHotbar = false;

        @Comment("The chance (%) to keep each item in your hotbar.")
        public double keepHotbarChance = 100;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class Armor {
        @Comment("Whether you should keep your armor when respawning at an obelisk.")
        public boolean keepArmor = false;

        @Comment("The chance (%) to keep each armor piece.")
        public double keepArmorChance = 100;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class Offhand {
        @Comment("Whether you should keep your offhand item when respawning at an obelisk.")
        public boolean keepOffhand = false;

        @Comment("The chance (%) to keep the item in your offhand.")
        public double keepOffhandChance = 100;
    }
}
