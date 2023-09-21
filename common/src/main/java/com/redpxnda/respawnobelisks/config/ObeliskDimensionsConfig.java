package com.redpxnda.respawnobelisks.config;

import com.redpxnda.respawnobelisks.RespawnObelisks;
import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

//todo lang
@Category(id = "dimensions", translation = "text.respawnobelisks.config.dimensions_config")
public final class ObeliskDimensionsConfig {
    private static final Logger LOGGER = RespawnObelisks.getLogger("Dimensions Config");
    private static List<ResourceLocation> overworldDimensions = null;
    private static List<ResourceLocation> netherDimensions = null;
    private static List<ResourceLocation> endDimensions = null;

    @ConfigEntry(
            id = "overworldObeliskDimensions",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.overworldObeliskDimensions"
    )
    @Comment("A whitelist of dimensions (by id) overworld respawn obelisks can be used in.")
    public static String[] overworldObeliskDimensions = {"minecraft:overworld"};

    @ConfigEntry(
            id = "netherObeliskDimensions",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.netherObeliskDimensions"
    )
    @Comment("A whitelist of dimensions (by id) nether respawn obelisks can be used in.")
    public static String[] netherObeliskDimensions = {"minecraft:the_nether"};

    @ConfigEntry(
            id = "endObeliskDimensions",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.netherObeliskDimensions"
    )
    @Comment("A whitelist of dimensions (by id) end respawn obelisks can be used in.")
    public static String[] endObeliskDimensions = {"minecraft:the_end"};

    @ConfigEntry(
            id = "dimensionsAsBlacklist",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.dimensionsAsBlacklist"
    )
    @Comment("Whether the '...ObeliskDimensions' fields should act as blacklists instead of whitelists.")
    public static boolean dimensionsAsBlacklist = false;

    private static void setupList(Level level, List<ResourceLocation> dimensions, String[] raw) {
        if (level.getServer() != null) {
            level.getServer().registryAccess().registry(Registries.DIMENSION_TYPE).ifPresent(r -> {
                for (String str : raw) {
                    ResourceLocation rl = ResourceLocation.tryParse(str);
                    if (rl == null) LOGGER.error("Found invalid id format used as dimension '{}' used in dimension config, ignoring.", str);
                    else dimensions.add(rl);
                }
            });
        }
    }

    public static boolean isValidOverworld(Level level) {
        if (overworldDimensions == null) {
            overworldDimensions = new ArrayList<>();
            setupList(level, overworldDimensions, overworldObeliskDimensions);
        }

        return dimensionsAsBlacklist != overworldDimensions.contains(level.dimension().location());
    }
    public static boolean isValidNether(Level level) {
        if (netherDimensions == null) {
            netherDimensions = new ArrayList<>();
            setupList(level, netherDimensions, netherObeliskDimensions);
        }

        return dimensionsAsBlacklist != netherDimensions.contains(level.dimension().location());
    }
    public static boolean isValidEnd(Level level) {
        if (endDimensions == null) {
            endDimensions = new ArrayList<>();
            setupList(level, endDimensions, endObeliskDimensions);
        }

        return dimensionsAsBlacklist != endDimensions.contains(level.dimension().location());
    }
}
