package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.codec.tag.EntityTypeList;
import com.redpxnda.nucleus.util.Comment;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.List;

@ConfigAutoCodec.ConfigClassMarker
public class ReviveConfig {
    @Comment("Whether entity revival via respawn obelisk should be enabled.")
    public boolean enableRevival = true;

    @Comment("Item used to revive an obelisk's saved entities.")
    public Item revivalItem = Items.TOTEM_OF_UNDYING;

    @Comment("Max number of entities that can be revived at once.")
    public int maxEntities = 3;

    @Comment("""
        Whitelist for all entities that can be revived. (Tags are supported)
        Any entry beginning with '$' is a hardcoded option that allows for the respective type to pass through.
        Available '$'s:
        '$tamables' (any tamable entity),
        '$animals' (pigs, cows, sheep, etc.),
        '$merchants' (villagers)"""
    )
    public EntityTypeList revivableEntities = new EntityTypeList(List.of(), List.of(), List.of("tamables", "animals", "merchants"));
    public boolean isEntityListed(Entity entity) {
        return entitiesIsBlacklist != revivableEntities.contains(entity);
    }

    @Comment("Whether the revivable entities list should act as a blacklist.")
    public boolean entitiesIsBlacklist = false;

    @Comment("Obelisk radiance depletion amount when reviving entities.")
    public double revivalCost = 10;
}
