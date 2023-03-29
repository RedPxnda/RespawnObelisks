package com.redpxnda.respawnobelisks.config;

import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.Config;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;
import com.teamresourceful.resourcefulconfig.web.annotations.WebInfo;

@Config("respawnobelisks")
@WebInfo(icon = "smartphone-charging")
public final class ServerConfig {
    @ConfigEntry(
        id = "obeliskChargeItems",
        type = EntryType.STRING,
        translation = "text.resourcefulconfig.respawnobelisks.option.server.obeliskChargeItems"
    )
    @Comment("""
            The items used for charging the obelisk.
            Syntax: ["<ITEM_ID>|<CHARGE_AMOUNT>", ...]
                    ["<ITEM_ID>|<CHARGE_AMOUNT>|<ALLOW_OVERFILL>", ...]
            Ex:     ["minecraft:stick|-1|false"]
            The 'ALLOW_OVERFILL' option is a boolean that determines whether the player
            is allowed to waste their item in order to get a portion of the actual charge amount.
            For example, if the charge amount is 3 and your obelisk is already charged twice,
            the charge amount WOULD go to 5. However, since the max is 3, it only goes to 3.
            The same logic is applied to going below 0 with negative charge values."""
    )
    public static String[] obeliskChargeItems = {"minecraft:ender_eye|25", "minecraft:ender_pearl|10"};


    @ConfigEntry(
            id = "netherObeliskChargeItems",
            type = EntryType.STRING,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.netherObeliskChargeItems"
    )
    public static String[] netherObeliskChargeItems = {"minecraft:ender_eye|25", "minecraft:ender_pearl|10"};


    @ConfigEntry(
            id = "endObeliskChargeItems",
            type = EntryType.STRING,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.endObeliskChargeItems"
    )
    public static String[] endObeliskChargeItems = {"minecraft:ender_eye|25", "minecraft:ender_pearl|10"};

    @ConfigEntry(
            id = "obeliskChargeSound",
            type = EntryType.STRING,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.obeliskChargeSound"
    )
    @Comment("Sound to play when charging an obelisk.")
    public static String obeliskChargeSound = "minecraft:block.respawn_anchor.charge";

    @ConfigEntry(
            id = "obeliskDepleteSound",
            type = EntryType.STRING,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.obeliskDepleteSound"
    )
    @Comment("Sound to play when de-charging or respawning at an obelisk.")
    public static String obeliskDepleteSound = "minecraft:block.respawn_anchor.deplete";

    @ConfigEntry(
            id = "obeliskSetSpawnSound",
            type = EntryType.STRING,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.obeliskSetSpawnSound"
    )
    @Comment("Sound to play when setting your spawn at an obelisk.")
    public static String obeliskSetSpawnSound = "minecraft:block.respawn_anchor.set_spawn";

    @ConfigEntry(
            id = "obeliskRemovalSound",
            type = EntryType.STRING,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.obeliskRemovalSound"
    )
    @Comment("Sound to play when removing an obelisk. See 'removalItem'.")
    public static String obeliskRemovalSound = "minecraft:block.beacon.deactivate";

    @ConfigEntry(
            id = "curseSound",
            type = EntryType.STRING,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.curseSound"
    )
    @Comment("Sound to play when respawning with the immortality curse.")
    public static String curseSound = "minecraft:entity.elder_guardian.curse";

    @ConfigEntry(
            id = "obeliskDepleteAmount",
            type = EntryType.INTEGER,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.obeliskDepleteAmount"
    )
    @Comment("How much charge (%) should be consumed when respawning at an obelisk.")
    public static int obeliskDepleteAmount = 20;

    @ConfigEntry(
            id = "infiniteChargeBlock",
            type = EntryType.STRING,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.infiniteChargeBlock"
    )
    @Comment("Block placed under an obelisk to allow for infinite charge.")
    public static String infiniteChargeBlock = "minecraft:beacon";

//    @ConfigEntry(
//            id = "allowPickup",
//            type = EntryType.BOOLEAN,
//            translation = "text.resourcefulconfig.respawnobelisks.option.server.allowPickup"
//    )
//    @Comment("Whether obelisk pickup should be allowed when removing an obelisk. See 'removalItem'.")
//    public static boolean allowPickup = true;

    @ConfigEntry(
            id = "revivalItem",
            type = EntryType.STRING,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.revivalItem"
    )
    @Comment("Item used to revive an obelisk's saved entities.")
    public static String revivalItem = "minecraft:totem_of_undying";

    @ConfigEntry(
            id = "revivalCostMultiplier",
            type = EntryType.DOUBLE,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.revivalCostMultiplier"
    )
    @Comment("Cost multiplier when reviving entities.")
    public static double revivalCostMultiplier = 0.5;

    @ConfigEntry(
            id = "enableCurse",
            type = EntryType.BOOLEAN,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.enableCurse"
    )
    @Comment("Whether the curse of immortality should be enabled or not.")
    public static boolean enableCurse = true;

    @ConfigEntry(
            id = "curseMaxLevel",
            type = EntryType.INTEGER,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.curseMaxLevel"
    )
    @Comment("Max level of the curse of immortality.")
    public static int curseMaxLevel = 5;

    @ConfigEntry(
            id = "curseLevelIncrement",
            type = EntryType.INTEGER,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.curseLevelIncrement"
    )
    @Comment("How much each death should increment the immortality curse by.")
    public static int curseLevelIncrement = 2;

    @ConfigEntry(
            id = "curseDuration",
            type = EntryType.INTEGER,
            translation = "text.resourcefulconfig.respawnobelisks.option.server.curseDuration"
    )
    @Comment("How long the immortality curse should last after respawning.")
    public static int curseDuration = 6000;
}
