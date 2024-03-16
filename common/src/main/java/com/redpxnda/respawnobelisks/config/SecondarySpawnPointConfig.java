package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.codec.tag.TaggableBlock;
import com.redpxnda.nucleus.util.Comment;

import java.util.LinkedHashMap;

@ConfigAutoCodec.ConfigClassMarker
public class SecondarySpawnPointConfig {
    @Comment("Whether secondary spawn points should be enabled. Secondary respawn points can be used to have multiple respawn points, triggered at various times.")
    public boolean enableSecondarySpawnPoints = false;

    @Comment("Determines the overall maximum amount of secondary respawn points you can have. -1 is infinite.")
    public int overallMaxPoints = -1;

    @Comment("""
            Determines a per-block cap on the amount of respawn points you can have. -1 is infinite.
            Tags(which get treated as a single entry) are supported by beginning the entry with a #.
            Example: {"#minecraft:beds": 5, "respawnobelisks:respawn_obelisk": 5} // this would allow you to have 5 bed respawn points and 5 obelisk respawn points""")
    public LinkedHashMap<TaggableBlock, Integer> maxPointsPerBlock = new LinkedHashMap<>();

    @Comment("Determines the maximum amount of secondary respawn points you can have at any block not specified in 'maxPointsPerBlock'. -1 is infinite.")
    public int defaultMaxPoints = -1;
}
