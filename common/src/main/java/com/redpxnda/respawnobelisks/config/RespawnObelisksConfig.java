package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.config.preset.ConfigPreset;
import com.redpxnda.nucleus.util.Comment;

@ConfigAutoCodec.ConfigClassMarker
public class RespawnObelisksConfig {
    public static RespawnObelisksConfig INSTANCE = new RespawnObelisksConfig();

    @Comment("""
             WARNING: OVERRIDES ALL OTHER VALUES - Quickly determines values for the config. Available presets:
             FORGIVING: A more forgiving preset with less expense and less difficulty.
             VANILLA_COMPLEMENTARY: A preset to make the mod tack on to vanilla's respawning rather than completely overriding it.
                                    Allows players to have multiple respawn points and makes obelisks give passive respawn perks.
             OBELISKS_AS_CHECKPOINTS: A preset to make obelisks act as "checkpoints."
                                      Players can have multiple respawn points, but obelisks take priority.""")
    public ConfigPreset<RespawnObelisksConfig, ConfigPresets> preset = ConfigPreset.none();

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
