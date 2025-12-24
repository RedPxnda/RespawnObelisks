package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Comment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

@ConfigAutoCodec.ConfigClassMarker
public class DimensionsConfig {
    @Comment("A whitelist of dimensions (by id) overworld respawn obelisks can be used in.")
    public List<ResourceLocation> overworldObeliskDimensions = new ArrayList<>(List.of(new ResourceLocation("minecraft:overworld")));

    @Comment("A whitelist of dimensions (by id) nether respawn obelisks can be used in.")
    public List<ResourceLocation> netherObeliskDimensions = new ArrayList<>(List.of(new ResourceLocation("minecraft:the_nether")));

    @Comment("A whitelist of dimensions (by id) end respawn obelisks can be used in.")
    public List<ResourceLocation> endObeliskDimensions = new ArrayList<>(List.of(new ResourceLocation("minecraft:the_end")));

    @Comment("Whether the '...ObeliskDimensions' fields should act as blacklists instead of whitelists.")
    public boolean dimensionsAsBlacklist = false;

    @Comment("""
            Determines where you should spawn at after teleporting through an end portal. Available options:
            WORLD_SPAWN: Teleports you to the world spawn.
            SET_SPAWN: Vanilla behavior; you will be sent to wherever your set spawn is, even if it's in the end.
            NON_END_SECONDARY: Only works if secondary respawn points are enabled. It teleports you to your first secondary spawn point that isn't in the end.
            WORLD_SPAWN_IF_IN_END: Teleports you to the world spawn if your set spawn is in the end, otherwise it will teleport to your spawn.""")
    public EndSpawnMode endSpawnMode = EndSpawnMode.WORLD_SPAWN_IF_IN_END;

    public enum EndSpawnMode {
        @Comment("World spawn will teleport you to the world spawn.")
        WORLD_SPAWN,
        @Comment("Set spawn is vanilla behavior; you will be sent to wherever your set spawn is.")
        SET_SPAWN,
        @Comment("Non-end secondary only works if secondary respawn points are enabled. It teleports you to your first secondary that isn't in the end.")
        NON_END_SECONDARY,
        @Comment("World spawn if in end teleports you to the world spawn if your set spawn is in the end.")
        WORLD_SPAWN_IF_IN_END
    }

    public boolean isValidOverworld(Level level) {
        return dimensionsAsBlacklist != overworldObeliskDimensions.contains(level.dimension().location());
    }
    public boolean isValidNether(Level level) {
        return dimensionsAsBlacklist != netherObeliskDimensions.contains(level.dimension().location());
    }
    public boolean isValidEnd(Level level) {
        return dimensionsAsBlacklist != endObeliskDimensions.contains(level.dimension().location());
    }
}
