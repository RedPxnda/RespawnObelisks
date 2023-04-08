package com.redpxnda.respawnobelisks.config;

import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

@Category(id = "perks", translation = "text.respawnobelisks.config.perks_config")
public final class RespawnPerkConfig {
    @ConfigEntry(
            id = "keepInventory",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.keep_inventory"
    )
    @Comment("Whether you should keep your inventory (discluding armor and offhand) when respawning at an obelisk.")
    public static boolean keepInventory = false;

    @ConfigEntry(
            id = "keepHotbar",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.keep_hotbar"
    )
    @Comment("Whether you should keep your hotbar when respawning at an obelisk.")
    public static boolean keepHotbar = false;

    @ConfigEntry(
            id = "keepOffhand",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.keep_offhand"
    )
    @Comment("Whether you should keep your offhand item when respawning at an obelisk.")
    public static boolean keepOffhand = false;

    @ConfigEntry(
            id = "keepArmor",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.keep_armor"
    )
    @Comment("Whether you should keep your armor when respawning at an obelisk.")
    public static boolean keepArmor = false;

    @ConfigEntry(
            id = "keepTrinkets",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.keep_trinkets"
    )
    @Comment("Whether you should keep your curios/trinkets (if installed) when respawning at an obelisk.")
    public static boolean keepTrinkets = false;
}
