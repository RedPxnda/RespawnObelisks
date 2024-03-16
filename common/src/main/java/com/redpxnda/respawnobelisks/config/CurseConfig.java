package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Comment;

@ConfigAutoCodec.ConfigClassMarker
public class CurseConfig {
    @Comment("Whether the curse of immortality should be enabled or not.")
    public boolean enableCurse = true;

    @Comment("Sound to play when respawning with the immortality curse.")
    public String curseSound = "minecraft:entity.elder_guardian.curse";

    @Comment("""
            Max level of the immortality curse.
            The player will be sent to world spawn the death after getting the max level."""
    )
    public int curseMaxLevel = 5;

    @Comment("How much each death should increment the immortality curse by.")
    public int curseLevelIncrement = 2;

    @Comment("How long the immortality curse should last after respawning. (In ticks)")
    public int curseDuration = 6000;
}
