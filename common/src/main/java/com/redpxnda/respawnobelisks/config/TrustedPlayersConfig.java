package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Comment;

@ConfigAutoCodec.ConfigClassMarker
public class TrustedPlayersConfig {
    @Comment("Whether players can be trusted/banned from accessing certain obelisks.")
    public boolean enablePlayerTrust = true;

    @Comment("Whether untrusted players can break the obelisk.")
    public boolean allowObeliskBreaking = false;

    @Comment("Whether untrusted players can respawn at the obelisk.")
    public boolean allowObeliskRespawning = false;

    @Comment("Whether untrusted players can interact with the obelisk.")
    public boolean allowObeliskInteraction = false;
}
