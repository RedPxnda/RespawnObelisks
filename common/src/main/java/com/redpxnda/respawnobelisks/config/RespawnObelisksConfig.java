package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Comment;

@ConfigAutoCodec.ConfigClassMarker
public class RespawnObelisksConfig {
    public static RespawnObelisksConfig INSTANCE = new RespawnObelisksConfig();

    @Comment("Whether players are allowed to respawn at an obelisk in hardcore mode.")
    public boolean allowHardcoreRespawning = true;

    public BehaviorOverridesConfig behaviorOverrides = new BehaviorOverridesConfig();

    public SecondarySpawnPointConfig secondarySpawnPoints = new SecondarySpawnPointConfig();

    public CurseConfig immortalityCurse = new CurseConfig();

    public RadianceConfig radiance = new RadianceConfig();

    public TeleportConfig teleportation = new TeleportConfig();

    public ReviveConfig revival = new ReviveConfig();

    public TrustedPlayersConfig playerTrusting = new TrustedPlayersConfig();

    public CoresConfig cores = new CoresConfig();

    public RespawnPerksConfig respawnPerks = new RespawnPerksConfig();

    public DimensionsConfig dimensions = new DimensionsConfig();
}
