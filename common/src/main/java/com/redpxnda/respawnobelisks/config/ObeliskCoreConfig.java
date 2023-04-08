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

@Category(id = "cores", translation = "text.respawnobelisks.config.cores_config")
public final class ObeliskCoreConfig {
    @ConfigEntry(
            id = "maxStoredEntities",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.max_stored_entities"
    )
    @Comment("Max number of revive entities that a core can hold.\nI recommend keeping this at a low amount, as large amounts (>10? Haven't tested values greater.) can cause issues with data storage.")
    public static int maxStoredEntities = 5;

    @ConfigEntry(
            id = "defaultOverworldCore",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.overworld_core"
    )
    @Comment("The default core item for overworld obelisks.\nIf you are a modder, datapacker, or modpacker, and want to add alternate core types, see the respective item tag.")
    public static String defaultOverworldCore = "respawnobelisks:obelisk_core";
    private static Supplier<Item> defaultOverworldCoreItem = null;
    public static Supplier<Item> getDefaultOverworldCoreItem() {
        if (defaultOverworldCoreItem == null) {
            ResourceLocation location = ResourceLocation.tryParse(defaultOverworldCore);
            defaultOverworldCoreItem = () -> Registry.ITEM.getOptional(location).orElse(ModRegistries.OBELISK_CORE.get());
        }
        return defaultOverworldCoreItem;
    }

    @ConfigEntry(
            id = "defaultNetherCore",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.nether_core"
    )
    @Comment("The default core item for nether obelisks.\nIf you are a modder, datapacker, or modpacker, and want to add alternate core types, see the respective item tag.")
    public static String defaultNetherCore = "respawnobelisks:obelisk_core_nether";
    private static Supplier<Item> defaultNetherCoreItem = null;
    public static Supplier<Item> getDefaultNetherCoreItem() {
        if (defaultNetherCoreItem == null) {
            ResourceLocation location = ResourceLocation.tryParse(defaultNetherCore);
            defaultNetherCoreItem = () -> Registry.ITEM.getOptional(location).orElse(ModRegistries.OBELISK_CORE_NETHER.get());
        }
        return defaultNetherCoreItem;
    }

    @ConfigEntry(
            id = "defaultEndCore",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.end_core"
    )
    @Comment("The default core item for end obelisks.\nIf you are a modder, datapacker, or modpacker, and want to add alternate core types, see the respective item tag.")
    public static String defaultEndCore = "respawnobelisks:obelisk_core_end";
    private static Supplier<Item> defaultEndCoreItem = null;
    public static Supplier<Item> getDefaultEndCoreItem() {
        if (defaultEndCoreItem == null) {
            ResourceLocation location = ResourceLocation.tryParse(defaultEndCore);
            defaultEndCoreItem = () -> Registry.ITEM.getOptional(location).orElse(ModRegistries.OBELISK_CORE_END.get());
        }
        return defaultEndCoreItem;
    }
}
