package com.redpxnda.respawnobelisks.config;

import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.trading.Merchant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Category(id = "teleport", translation = "text.respawnobelisks.config.teleport_config")
public final class TeleportConfig {
    @ConfigEntry(
            id = "enableTeleportation",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.enable_teleportation"
    )
    @Comment("Whether players can teleport to obelisks by binding a recovery compass to a lodestone under the obelisk.")
    public static boolean enableTeleportation = true;

    @ConfigEntry(
            id = "teleportationCooldown",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.tp_cooldown"
    )
    @Comment("The delay before being able to teleport again. (In ticks)\nKeep this above 100, otherwise issues will arise.\nDefault value: 3 minutes/3600 ticks")
    public static int teleportationCooldown = 3600;

    @ConfigEntry(
            id = "teleportationBackupCooldown",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.tp_backup_cooldown"
    )
    @Comment("The delay before being able to teleport again after an unsuccessful teleport. (Eg. player moves or switches items, etc.")
    public static int teleportationBackupCooldown = 200;

    @ConfigEntry(
            id = "dropItemsOnTeleport",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.drop_on_tp"
    )
    @Comment("Whether players drop their items when teleporting. If enabled, all enabled perks in the 'perks' section will apply.")
    public static boolean dropItemsOnTeleport = false;

    @ConfigEntry(
            id = "dropCompassOnTp",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.drop_compass"
    )
    @Comment("Whether players drop their recovery compass when teleporting.")
    public static boolean dropCompassOnTp = true;

    @ConfigEntry(
            id = "xpCost",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.xp_cost"
    )
    @Comment("The amount of experience(points, not levels) consumed when teleporting to an obelisk.\nUseful numbers: https://minecraft.fandom.com/wiki/Experience#Leveling_up (See 'Total XP')\nDefault value: 27 points/3 levels")
    public static int xpCost = 27;

    @ConfigEntry(
            id = "levelCost",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.level_cost"
    )
    @Comment("The amount of experience(levels, not points) consumed when teleporting to an obelisk.\nThis is different to 'xpCost' as the cost to teleport will technically get more expensive the more levels you have.\nThis is similar to how Waystones works.")
    public static int levelCost = 0;

    @ConfigEntry(
            id = "allowCursedTeleportation",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.allow_cursed_tp"
    )
    @Comment("Whether players can teleport whilst having the immortality curse.")
    public static boolean allowCursedTeleportation = false;

    @ConfigEntry(
            id = "forcedCurseOnTp",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.forced_curse_tp"
    )
    @Comment("Whether the immortality curse should be forcefully applied when teleporting.\nRegardless of this value, you will still receive the curse when teleporting to uncharged obelisks.")
    public static boolean forcedCurseOnTp = false;

    @ConfigEntry(
            id = "minimumTpCharge",
            type = EntryType.DOUBLE,
            translation = "text.respawnobelisks.config.minimum_tp_charge"
    )
    @Comment("Minimum charge required to teleport. Values of 0 or lower will disable this requirement.")
    public static double minimumTpCharge = 1;

    @ConfigEntry(
            id = "teleportationChargeCost",
            type = EntryType.DOUBLE,
            translation = "text.respawnobelisks.config.teleportation_cost"
    )
    @Comment("Amount of charge lost when teleporting to an obelisk.")
    public static double teleportationChargeCost = 20;
}
