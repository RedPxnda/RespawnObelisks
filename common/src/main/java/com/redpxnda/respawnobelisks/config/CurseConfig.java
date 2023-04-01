package com.redpxnda.respawnobelisks.config;

import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;

@Category(id = "immortalityCurse", translation = "text.respawnobelisks.config.curse_config")
public final class CurseConfig {
    @ConfigEntry(
            id = "enableCurse",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.enable_curse"
    )
    @Comment("Whether the curse of immortality should be enabled or not.")
    public static boolean enableCurse = true;

    @ConfigEntry(
            id = "curseSound",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.curse_sound"
    )
    @Comment("Sound to play when respawning with the immortality curse.")
    public static String curseSound = "minecraft:entity.elder_guardian.curse";

    @ConfigEntry(
            id = "curseMaxLevel",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.curse_max_level"
    )
    @Comment("""
            Max level of the immortality curse.
            The player will be sent to world spawn the death after getting the max level."""
    )
    public static int curseMaxLevel = 5;

    @ConfigEntry(
            id = "curseLevelIncrement",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.curse_increment"
    )
    @Comment("How much each death should increment the immortality curse by.")
    public static int curseLevelIncrement = 2;

    @ConfigEntry(
            id = "curseDuration",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.curse_duration"
    )
    @Comment("How long the immortality curse should last after respawning. (In ticks)")
    public static int curseDuration = 6000;
}
