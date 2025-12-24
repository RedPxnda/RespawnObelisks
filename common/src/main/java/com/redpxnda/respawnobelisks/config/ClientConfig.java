package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Comment;

@ConfigAutoCodec.ConfigClassMarker
public class ClientConfig {
    @Comment("Minimum texture x value for obelisk rune rendering.")
    public int minTextureX = 3;

    @Comment("Maximum texture x value for obelisk rune rendering.")
    public int maxTextureX = 12;

    @Comment("Minimum texture y value for obelisk rune rendering.")
    public int minTextureY = 2;

    @Comment("Maximum texture y value for obelisk rune rendering.")
    public int maxTextureY = 14;

    @Comment("How far inwards/outwards the runes are from the obelisk(in pixels).")
    public float zPos = 5.505f;
}
