package com.redpxnda.respawnobelisks.config;

import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Category(id = "revive", translation = "text.respawnobelisks.config.revive_config")
public final class ReviveConfig {
    private static List<EntityType<?>> listedEntities = new ArrayList<>();

    @ConfigEntry(
            id = "enableRevival",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.enable_revival"
    )
    @Comment("Whether entity revival via respawn obelisk should be enabled.")
    public static boolean enableRevival = true;

    @ConfigEntry(
            id = "revivalItem",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.revival_item"
    )
    @Comment("Item used to revive an obelisk's saved entities.")
    public static String revivalItem = "minecraft:totem_of_undying";

    @ConfigEntry(
            id = "maxRevivalEntities",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.max_revive_entities"
    )
    @Comment("Max number of entities that can be revived at once.")
    public static int maxEntities = 3;

    @ConfigEntry(
            id = "revivableEntities",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.revivable_entities"
    )
    @Comment("""
        Whitelist for all entities that can be revived. (Tags are supported)
        Any entry beginning with '$' is a hardcoded option that allows for the respective type to pass through.
        Available '$'s:
        '$tamables' (any tamable entity),
        '$animals' (pigs, cows, sheep, etc.),
        '$merchants' (villagers)"""
    )
    public static String[] revivableEntities = {"$tamables", "$animals", "$merchants"};
    public static boolean isEntityListed(Entity entity) {
        if (listedEntities.isEmpty()) {
            for (String str : revivableEntities) {
                if (str.startsWith("#")) {
                    str = str.substring(1);
                    ResourceLocation loc = ResourceLocation.tryParse(str);
                    if (loc == null) continue;
                    TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, loc);
                    if (BuiltInRegistries.ENTITY_TYPE.getTag(tag).isPresent()) {
                        for (Holder<EntityType<?>> entityTypeHolder : BuiltInRegistries.ENTITY_TYPE.getTag(tag).get()) {
                            listedEntities.add(entityTypeHolder.value());
                        }
                    }
                } else {
                    ResourceLocation loc = ResourceLocation.tryParse(str);
                    if (loc == null) continue;
                    BuiltInRegistries.ENTITY_TYPE.getOptional(loc).ifPresent(e -> listedEntities.add(e));
                }
            }
        }
        List<String> entities = Arrays.asList(revivableEntities);
        boolean result = listedEntities.contains(entity.getType()) ||
                (entities.contains("$tamables") && entity instanceof OwnableEntity) ||
                (entities.contains("$animals") && entity instanceof Animal) ||
                (entities.contains("$merchants") && entity instanceof Merchant);
        return entitiesIsBlacklist != result;
    }

    @ConfigEntry(
            id = "revivableEntitiesBlacklist",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.revivable_entities_blacklist"
    )
    @Comment("Whether the revivable entities list should act as a blacklist.")
    public static boolean entitiesIsBlacklist = false;

    @ConfigEntry(
            id = "revivalCost",
            type = EntryType.DOUBLE,
            translation = "text.respawnobelisks.config.revival_cost"
    )
    @Comment("Obelisk depletion amount when reviving entities.")
    public static double revivalCost = 10;
}
