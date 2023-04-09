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
