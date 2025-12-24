package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.codec.tag.BlockList;
import com.redpxnda.nucleus.codec.tag.TaggableBlock;
import com.redpxnda.nucleus.util.Comment;
import com.redpxnda.respawnobelisks.registry.block.entity.RadiantFlameBlockEntity;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import java.util.LinkedHashMap;
import net.minecraft.server.level.ServerPlayer;

@ConfigAutoCodec.ConfigClassMarker
public class SecondarySpawnPointConfig {
    @Comment("Whether secondary spawn points should be enabled. Secondary respawn points can be used to have multiple respawn points, triggered at various times.")
    public boolean enableSecondarySpawnPoints = false;

    @Comment("Determines the overall maximum amount of secondary respawn points you can have. -1 is infinite.")
    public int overallMaxPoints = 5;

    @Comment("""
            Determines a per-block cap on the amount of respawn points you can have. -1 is infinite.
            Tags(which get treated as a single entry) are supported by beginning the entry with a #.
            Example: {"#minecraft:beds": 5, "respawnobelisks:respawn_obelisk": 5} // this would allow you to have 5 bed respawn points and 5 obelisk respawn points""")
    public LinkedHashMap<TaggableBlock, Integer> maxPointsPerBlock = new LinkedHashMap<>();

    @Comment("Determines the maximum amount of secondary respawn points you can have at any block not specified in 'maxPointsPerBlock'. -1 is infinite.")
    public int defaultMaxPoints = -1;

    @Comment("Whether players can forcefully change the order of their secondary respawn points by shift right clicking a respawn point.")
    public boolean allowPriorityShifting = true;

    @Comment("Whether blocks should be given a specific order for respawning. This will not work well with 'allowPriorityShifting.'")
    public boolean enableBlockPriorities = false;

    @Comment("""
            Determines a per-block priority in which to order secondary respawn points. Default value is 0. Higher numbers have higher priority.
            Tags(which get treated as a single entry) are supported by beginning the entry with a #.""")
    public LinkedHashMap<TaggableBlock, Float> blockPriorities = new LinkedHashMap<>();

    public static final String spawnModeComment = """
            NEVER: Players can never choose.
            ALWAYS: Players can always choose.
            IF_CHARGED: Players can only choose if their respawn point is an obelisk that has charge.
            UNLESS_CHARGED: Players can only choose if their respawn point is an obelisk that does not have charge.
            UNLESS_CHARGED_OBELISK: Players can only choose if their obelisk does not have charge, or if their respawn point isn't an obelisk.
            IF_OBELISK: Players can only choose if their respawn point is an obelisk.
            UNLESS_OBELISK: Players can only choose if their respawn point is not an obelisk.
            IF_FLAME: Players can only choose if their respawn point is a radiant flame.
            UNLESS_FLAME: Players can only choose if their respawn point is not a radiant flame.
            """;

    @Comment("Whether attempting to set your spawn point when you are unable to overrides an existing spawn point.")
    public boolean forceSpawnSetting = false;

    @Comment("When players are allowed to choose to spawn at a secondary respawn point.\n" + spawnModeComment)
    public PointSpawnMode secondarySpawnMode = PointSpawnMode.NEVER;

    @Comment("A list of block ids which players are not allowed to choose to respawn at, unless it is their main respawn point.")
    public BlockList secondarySpawnBlockBlacklist = BlockList.of();

    @Comment("Whether 'secondarySpawnBlockBlacklist' should act as a whitelist instead of a blacklist.")
    public boolean secondarySpawnBlocksAsWhitelist = false;

    @Comment("When players are allowed to choose to respawn at world spawn.\n" + spawnModeComment)
    public PointSpawnMode worldSpawnMode = PointSpawnMode.NEVER;

    public enum PointSpawnMode {
        NEVER,
        ALWAYS,
        IF_CHARGED,
        UNLESS_CHARGED,
        UNLESS_CHARGED_OBELISK,
        IF_OBELISK,
        UNLESS_OBELISK,
        IF_FLAME,
        UNLESS_FLAME;

        public boolean evaluate(SpawnPoint point, ServerPlayer player) {
            return switch (this) {
                case IF_CHARGED -> point != null && player.getServer().getLevel(point.dimension()).getBlockEntity(point.pos()) instanceof RespawnObeliskBlockEntity robe && robe.getCharge(player)-robe.getCost(player) >= 0;
                case UNLESS_CHARGED_OBELISK -> point == null || !(player.getServer().getLevel(point.dimension()).getBlockEntity(point.pos()) instanceof RespawnObeliskBlockEntity robe) || robe.getCharge(player)-robe.getCost(player) < 0;
                case UNLESS_CHARGED -> point != null && player.getServer().getLevel(point.dimension()).getBlockEntity(point.pos()) instanceof RespawnObeliskBlockEntity robe && robe.getCharge(player)-robe.getCost(player) < 0;
                case IF_OBELISK -> point != null && player.getServer().getLevel(point.dimension()).getBlockEntity(point.pos()) instanceof RespawnObeliskBlockEntity;
                case UNLESS_OBELISK -> point == null || !(player.getServer().getLevel(point.dimension()).getBlockEntity(point.pos()) instanceof RespawnObeliskBlockEntity);
                case IF_FLAME -> point != null && player.getServer().getLevel(point.dimension()).getBlockEntity(point.pos()) instanceof RadiantFlameBlockEntity;
                case UNLESS_FLAME -> point == null || !(player.getServer().getLevel(point.dimension()).getBlockEntity(point.pos()) instanceof RadiantFlameBlockEntity);
                case NEVER -> false;
                case ALWAYS -> true;
            };
        }
    }
}
