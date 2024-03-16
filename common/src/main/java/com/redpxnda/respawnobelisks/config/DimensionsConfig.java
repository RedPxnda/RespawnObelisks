package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Comment;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

@ConfigAutoCodec.ConfigClassMarker
public class DimensionsConfig {
    @Comment("A whitelist of dimensions (by id) overworld respawn obelisks can be used in.")
    public List<Identifier> overworldObeliskDimensions = new ArrayList<>(List.of(new Identifier("minecraft:overworld")));

    @Comment("A whitelist of dimensions (by id) nether respawn obelisks can be used in.")
    public List<Identifier> netherObeliskDimensions = new ArrayList<>(List.of(new Identifier("minecraft:the_nether")));

    @Comment("A whitelist of dimensions (by id) end respawn obelisks can be used in.")
    public List<Identifier> endObeliskDimensions = new ArrayList<>(List.of(new Identifier("minecraft:the_end")));

    @Comment("Whether the '...ObeliskDimensions' fields should act as blacklists instead of whitelists.")
    public boolean dimensionsAsBlacklist = false;

    @Comment("""
            Determines where you should spawn at after teleporting through an end portal. Available options:
            WORLD_SPAWN: Teleports you to the world spawn.
            SET_SPAWN: Vanilla behavior; you will be sent to wherever your set spawn is.
            NON_END_SECONDARY: Only works if secondary respawn points are enabled. It teleports you to your first secondary that isn't in the end.""")
    public EndSpawnMode endSpawnMode = EndSpawnMode.WORLD_SPAWN;

    public enum EndSpawnMode {
        @Comment("World spawn will teleport you to the world spawn.")
        WORLD_SPAWN,
        @Comment("Set spawn is vanilla behavior; you will be sent to wherever your set spawn is.")
        SET_SPAWN,
        @Comment("Non-end secondary only works if secondary respawn points are enabled. It teleports you to your first secondary that isn't in the end.")
        NON_END_SECONDARY
    }

    public boolean isValidOverworld(World level) {
        return dimensionsAsBlacklist != overworldObeliskDimensions.contains(level.getRegistryKey().getValue());
    }
    public boolean isValidNether(World level) {
        return dimensionsAsBlacklist != netherObeliskDimensions.contains(level.getRegistryKey().getValue());
    }
    public boolean isValidEnd(World level) {
        return dimensionsAsBlacklist != endObeliskDimensions.contains(level.getRegistryKey().getValue());
    }
}
